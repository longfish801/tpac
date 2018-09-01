/*
 * TeaMaker.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.parser;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.ArgmentChecker;
import io.github.longfish801.tpac.TeaServer;
import io.github.longfish801.tpac.element.TeaDec;
import io.github.longfish801.tpac.element.TeaHandle;
import io.github.longfish801.tpac.element.TpacDec;
import io.github.longfish801.tpac.element.TpacHandle;
import io.github.longfish801.tpac.element.TpacScalar;
import io.github.longfish801.tpac.element.TpacText;

/**
 * tpac記法の解析にともない、各要素を生成する特性です。<br>
 * tpac文書の読込は {@link TeaServer#soak(def)}を利用してください。<br>
 * 本クラスのメソッドは {@link TeaParty}での解析時に呼ばれます。
 * @version 1.0.00 2018/08/16
 * @author io.github.longfish801
 */
trait TeaMaker {
	/** TeaServer */
	TeaServer server;
	/** 生成中のTeaHandle */
	TeaHandle handle;
	/** テキストの格納先 */
	TeaParty.ParseStatus textStore;
	/** コメントの格納先 */
	TeaParty.ParseStatus commentStore;
	/** 最後に格納したコレクションの階層 */
	int preLevelCollection;
	/** 生成対象のコレクション */
	def curCollection;
	
	/**
	 * TeaMakerを準備します。<br/>
	 * インスタンス作成後に必ず実行してください。
	 * @param server TeaServer
	 * @return 自インスタンス
	 */
	TeaMaker setup(TeaServer server){
		ArgmentChecker.checkNotNull('TeaServer', server);
		this.server = server;
		return this;
	}
	
	/**
	 * このTeaMakerが解析対象とする宣言のタグを返します。
	 * @return 宣言のタグ
	 */
	String getDecTag(){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * TeaDecインスタンスを生成します。
	 * @param tag タグ
	 * @param name 名前
	 * @return TeaDec
	 */
	TeaDec newTeaDec(String tag, String name){
		return new TpacDec().setup(tag, name, server);
	}
	
	/**
	 * TeaHandleインスタンスを生成します。
	 * @param tag タグ
	 * @param name 名前
	 * @param upper 上位ハンドル
	 * @return TeaHandle
	 */
	TeaHandle newTeaHandle(String tag, String name, TeaHandle upper){
		return new TpacHandle().setup(tag, name, upper);
	}
	
	/**
	 * 文字列をスカラー値に変換します。
	 * @param raw 文字列
	 * @return スカラー値
	 */
	def evalScalar(String raw){
		if (raw == null) return null;
		if (handle == null) throw new TeaMakerMakeException("スカラー値を関連付けるハンドルがありません。raw=${raw}");
		return TpacScalar.eval(raw, handle);
	}
	
	/**
	 * テキスト格納用のインスタンスを生成します。
	 * @return テキスト格納用のインスタンス
	 */
	List newTeaText(){
		return new TpacText();
	}
	
	/**
	 * 宣言を生成します。
	 * @param tag タグ
	 * @param name 名前
	 * @param scalar スカラー値
	 */
	void createDec(String tag, String name, String scalar){
		TeaDec dec = this.newTeaDec(tag, name);
		handle = dec;
		if (scalar != null) dec.scalar = evalScalar(scalar);
		textStore = TeaParty.ParseStatus.HANDLE;
		commentStore = TeaParty.ParseStatus.HANDLE;
		preLevelCollection = 0;
	}
	
	/**
	 * 宣言終端を生成します。
	 */
	void createDecEnd(){
		textStore = TeaParty.ParseStatus.OUT;
		commentStore = TeaParty.ParseStatus.OUT;
		handle.validate();
	}
	
	/**
	 * ハンドルを生成します。
	 * @param tag タグ
	 * @param name 名前
	 * @param level 階層
	 * @param scalar スカラー値
	 */
	void createHandle(String tag, String name, int level, String scalar){
		if (handle == null) throw new TeaMakerMakeException("ハンドルを関連付ける上位ハンドルがありません。tag=${tag}, name=${name}, level=${level}");
		if (handle.level - level < -1){
			throw new TeaMakerMakeException("ハンドルの階層はひとつずつ上げる必要があります。tag=${tag}, name=${name}, level=${level}");
		}
		Closure higherHandle;	// 指定された数だけ上位の階層にあるハンドルを返します
		higherHandle = { int diff, TeaHandle hndl ->
			return (diff == 0)? hndl : higherHandle(diff - 1, hndl.upper);
		}
		TeaHandle upper;
		switch (handle.level - level){
			case -1: upper = handle; break;
			case 0: upper = handle.upper; break;
			default: upper = higherHandle(handle.level - level, handle.upper);
		}
		TeaHandle newHandle = this.newTeaHandle(tag, name, upper);
		handle = newHandle;
		if (scalar != null) newHandle.scalar = evalScalar(scalar);
		textStore = TeaParty.ParseStatus.HANDLE;
		commentStore = TeaParty.ParseStatus.HANDLE;
		preLevelCollection = 0;
	}
	
	/**
	 * ハンドル終端を生成します。
	 */
	void createHandleEnd(){
		textStore = TeaParty.ParseStatus.OUT;
		commentStore = TeaParty.ParseStatus.OUT;
	}
	
	/**
	 * テキストを生成します。
	 * @param line テキスト行
	 */
	void createText(String line){
		if (handle == null) throw new TeaMakerMakeException("テキストを関連付ける上位ハンドルがありません。tag=${tag}, name=${name}, level=${level}");
		switch (textStore){
			case TeaParty.ParseStatus.HANDLE:
				if (handle.text == null) handle.text = newTeaText();
				handle.text << line;
				commentStore = TeaParty.ParseStatus.TEXT;
				break;
			case TeaParty.ParseStatus.LIST:
				referCollection(preLevelCollection + 1, curCollection, { return newTeaText() }) << line;
				break;
			case TeaParty.ParseStatus.MAP:
				referCollection(preLevelCollection + 1, curCollection, { return newTeaText() }) << line;
				break;
			default:
				throw new InternalError("想定外の格納先です。textStore=${textStore}");
		}
	}
	
	/**
	 * リストを生成します。
	 * @param scalar スカラー値
	 * @param level 階層
	 */
	void createList(String scalar, int level){
		if (handle == null) throw new TeaMakerMakeException("リストを関連付ける上位ハンドルがありません。scalar=${scalar} level=${level}");
		if (preLevelCollection - level < -1){
			throw new TeaMakerMakeException("リストの階層はひとつずつ上げる必要があります。handle=${handle.key} scalar=${scalar} level=${level}");
		}
		if (level == 0){
			curCollection = handle.list;
			commentStore = TeaParty.ParseStatus.LIST;
		}
		referCollection(level, curCollection, { return [] }) << evalScalar(scalar);
		preLevelCollection = level;
		textStore = (scalar == null)? TeaParty.ParseStatus.LIST : TeaParty.ParseStatus.HANDLE;
	}
	
	/**
	 * マップを生成します。
	 * @param key キー
	 * @param scalar スカラー値
	 * @param level 階層
	 */
	void createMap(String key, String scalar, int level){
		if (handle == null) throw new TeaMakerMakeException("マップを関連付ける上位ハンドルがありません。key=${key} level=${level}");
		if (preLevelCollection - level < -1){
			throw new TeaMakerMakeException("マップの階層はひとつずつ上げる必要があります。handle=${handle.key} key=${key} level=${level}");
		}
		if (level == 0){
			curCollection = handle.map;
			commentStore = TeaParty.ParseStatus.MAP;
		}
		referCollection(level, curCollection, { return [:] }).put(key, evalScalar(scalar));
		preLevelCollection = level;
		textStore = (scalar == null)? TeaParty.ParseStatus.MAP : TeaParty.ParseStatus.HANDLE;
	}
	
	/**
	 * コメントを生成します。
	 * @param line コメント行
	 */
	void createComment(String line){
		if (handle == null) throw new TeaMakerMakeException("マップを関連付ける上位ハンドルがありません。key=${key} level=${level}");
		switch (commentStore){
			case TeaParty.ParseStatus.HANDLE:
				handle.comment.handle << line;
				break;
			case TeaParty.ParseStatus.LIST:
				handle.comment.list << line;
				break;
			case TeaParty.ParseStatus.MAP:
				handle.comment.map << line;
				break;
			case TeaParty.ParseStatus.TEXT:
				handle.comment.text << line;
				break;
			default:
				throw new InternalError("想定外の格納先です。commentStore=${commentStore}");
		}
	}
	
	/**
	 * 指定した階層のコレクションを返します。
	 * @param level 階層
	 * @param collec 参照するリストあるいはマップ
	 * @param initLower 下位の階層を初期化するクロージャ
	 * @return 指定した階層のコレクション
	 */
	def referCollection(int level, def collec, Closure initLower){
		if (collec == null) throw new TeaMakerMakeException("格納先のコレクションが作成されていません。level=${level}");
		if (level == 0) return collec;
		def elem = (collec instanceof List)? collec.last() : collec.values().last();
		if (elem == null){
			collec[(collec instanceof List)? collec.size() - 1 : collec.keySet().last()] = initLower();
			elem = (collec instanceof List)? collec.last() : collec.values().last();
		}
		return referCollection(level - 1, elem, initLower);
	}
}
