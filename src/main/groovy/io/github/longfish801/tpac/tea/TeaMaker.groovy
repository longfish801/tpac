/*
 * TeaMaker.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.tea

import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.TpacDec
import io.github.longfish801.tpac.TpacEval
import io.github.longfish801.tpac.TpacHandle
import io.github.longfish801.tpac.TpacMsg as msgs
import io.github.longfish801.tpac.TpacRefer
import io.github.longfish801.tpac.TpacSemanticException
import java.util.regex.Pattern
import org.apache.commons.text.StringEscapeUtils

/**
 * tpac記法の解析にともない、各要素を生成する特性です。<br/>
 * 本クラスのメソッドは {@link TeaParty}での解析時に呼ばれます。<br/>
 * 独自DSLのため実装するときは {@link #newTeaDec(String,String)}、
 * {@link #newTeaHandle(String,String,TeaHandle)}を
 * オーバーライドしてください。
 * @version 0.3.09 2021/10/26
 * @author io.github.longfish801
 */
trait TeaMaker {
	/** 解析中のハンドル */
	TeaHandle handle
	/** テキストの格納先となるマップのキー */
	String keyForText
	
	/**
	 * 宣言を生成します。<br/>
	 * タグと名前の設定は不要です。<br/>
	 * タグと名前の設定は本メソッドの呼出元である
	 * {@link #createDec(String,String,String)}で実行します。
	 * @param tag タグ
	 * @param name 名前
	 * @return 宣言
	 */
	TeaDec newTeaDec(String tag, String name){
		return new TpacDec()
	}
	
	/**
	 * ハンドルを生成します。
	 * タグと名前、上位ハンドルの設定は不要です。<br/>
	 * タグと名前、上位ハンドルの設定は本メソッドの呼出元である
	 * {@link #createHandle(String,String,int,String)}で実行します。
	 * @param tag タグ
	 * @param name 名前
	 * @param upper 上位ハンドル
	 * @return ハンドル
	 */
	TeaHandle newTeaHandle(String tag, String name, TeaHandle upper){
		return new TpacHandle()
	}
	
	/**
	 * 生成した宣言を返します。<br/>
	 * 解析処理の完了後に呼んでください。
	 * @return 宣言
	 */
	TeaDec getDec(){
		return handle.dec
	}
	
	/**
	 * 宣言の解析時に呼ばれます。
	 * @param tag タグ
	 * @param name 名前
	 * @param scalar スカラー値
	 */
	void createDec(String tag, String name, String scalar){
		handle = this.newTeaDec(tag, name)
		handle.tag = tag
		handle.name = name
		createMap(cnst.dflt.mapKey, scalar)
	}
	
	/**
	 * 宣言終端の解析時に呼ばれます。<br/>
	 * 現状、特に処理はなにもありません。
	 */
	void createDecEnd(){
	}
	
	/**
	 * ハンドルの解析時に呼ばれます。
	 * @param tag タグ
	 * @param name 名前
	 * @param level 階層
	 * @param scalar スカラー値
	 * @exception TpacSemanticException ハンドルの階層はひとつずつ上げる必要があります。
	 * @exception TpacSemanticException ハンドルの識別キーが重複しています。
	 */
	void createHandle(String tag, String name, int level, String scalar){
		if (level - handle.level > 1){
			throw new TpacSemanticException(String.format(msgs.exc.cannotSkipLevel, tag, name, level))
		}
		
		// 指定された数だけ上位の階層にあるハンドルを返すクロージャ
		Closure higherHandle
		higherHandle = { int diff, TeaHandle hndl ->
			return (diff == 0)? hndl : higherHandle(diff - 1, hndl.upper)
		}
		
		// 親ハンドルを取得する
		TeaHandle upper
		switch (level - handle.level){
			case 1: upper = handle; break
			case 0: upper = handle.upper; break
			default: upper = higherHandle(handle.level - level, handle.upper)
		}
		
		// ハンドルを作成する
		handle = this.newTeaHandle(tag, name, upper)
		handle.tag = tag
		handle.name = name
		if (upper.lowers[handle.keyNatural] != null){
			// 親ハンドルに同じキーが設定済（キーが重複）であれば例外を投げます
			throw new TpacSemanticException(String.format(msgs.exc.duplicateHandleKey, handle.key, upper.path))
		}
		upper << handle
		createMap(cnst.dflt.mapKey, scalar)
	}
	
	/**
	 * ハンドル終端の解析時に呼ばれます。<br/>
	 * 現状、特に処理はなにもありません。
	 */
	void createHandleEnd(){
	}
	
	/**
	 * テキストの解析時に呼ばれます。
	 * @param line テキスト行
	 * @param textLineNo テキスト行番号
	 * @exception TpacSemanticException テキストを関連付けるマップのキーが重複しています。
	 */
	void createText(String line, int textLineNo){
		if (textLineNo == 1){
			if (handle.map.containsKey(keyForText)){
				throw new TpacSemanticException(String.format(msgs.exc.duplicateMapKeyText, keyForText))
			}
			handle.setAt(keyForText, [])
		}
		handle[keyForText] << line
	}
	
	/**
	 * マップの解析時に呼ばれます。
	 * @param key キー
	 * @param scalar スカラー値
	 * @exception TpacSemanticException マップのキーが重複しています。
	 */
	void createMap(String key, String scalar){
		if (handle.map.containsKey(key)) throw new TpacSemanticException(String.format(msgs.exc.duplicateMapKey, key, scalar))
		if (scalar != null){
			handle.setAt(key, evalScalar(scalar))
			keyForText = cnst.dflt.mapKey
		} else {
			keyForText = key
		}
	}
	
	/**
	 * コメントの解析時に呼ばれます。
	 * @param line コメント行
	 */
	void createComment(String line){
		handle.comments << line
	}
	
	/**
	 * スカラー値の文字列表現をスカラー値に変換します。
	 * @param raw スカラー値の文字列表現
	 * @return スカラー値
	 */
	def evalScalar(String raw){
		switch (raw){
			case cnst.scalar.kwdNull:
				return null
			case cnst.scalar.kwdTrue:
				return true
			case cnst.scalar.kwdFalse:
				return false
			case {it ==~ cnst.scalar.numInt}:
				return Integer.parseInt(raw)
			case {it ==~ cnst.scalar.numBigDecimal}:
				return new BigDecimal(raw)
			case {it.startsWith(cnst.scalar.refer)}:
				return TpacRefer.newInstance(handle, raw.substring(cnst.scalar.refer.length()))
			case {it.startsWith(cnst.scalar.rex)}:
				return Pattern.compile(raw.substring(cnst.scalar.rex.length()))
			case {it.startsWith(cnst.scalar.eval)}:
				return new TpacEval(raw.substring(cnst.scalar.eval.length()))
			case {it.startsWith(cnst.scalar.str)}:
				return StringEscapeUtils.unescapeJava(raw.substring(cnst.scalar.str.length()))
		}
		return raw
	}
}
