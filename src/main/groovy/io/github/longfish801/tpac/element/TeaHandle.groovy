/*
 * TeaHandle.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.ArgmentChecker;
import io.github.longfish801.shared.ExchangeResource;
import io.github.longfish801.tpac.parser.TeaMakerMakeException;
import java.util.regex.Matcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 * tpac文書のハンドラ特性です。
 * @version 1.0.00 2018/08/10
 * @author io.github.longfish801
 */
@Slf4j('LOG')
trait TeaHandle {
	/** ConfigObject */
	static final ConfigObject cnstTeaHandle = ExchangeResource.config(TeaHandle.class);
	/** タグ */
	String tag;
	/** 名前 */
	String name;
	/** 上位ハンドル */
	TeaHandle upper;
	/** 識別キーと下位ハンドラとのマップ */
	Map<String, TeaHandle> lowers = [:];
	/** スカラー値 */
	def scalar;
	/** テキスト */
	TpacText text = [] as TpacText;
	/** リスト */
	List list = [];
	/** マップ */
	Map map = [:];
	/** コメント */
	Map comment = [
		handle : [],
		scalar : [],
		text : [],
		list : [],
		map : []
	];
	
	/**
	 * ハンドラを準備します。<br/>
	 * インスタンス作成後に必ず実行してください。
	 * @param tag タグ
	 * @param name 名前
	 * @param upper 上位ハンドル
	 * @return 自インスタンス
	 */
	TeaHandle setup(String tag, String name, TeaHandle upper) {
		ArgmentChecker.checkNotNull('上位ハンドル', upper);
		this.tag = tag;
		this.name = name;
		upper << this;
		return this;
	}
	
	/**
	 * 下位ハンドラを追加します。
	 * @param lower 下位ハンドラ
	 * @return 自インスタンス
	 */
	TeaHandle leftShift(TeaHandle lower){
		ArgmentChecker.checkNotNull('下位ハンドラ', lower);
		ArgmentChecker.checkUniqueKey('下位ハンドラ', lower.key, lowers);
		lowers[lower.key] = lower;
		lower.upper = this;
		return this;
	}
	
	/**
	 * このハンドラの妥当性を検証します。<br/>
	 * このメソッドは {@link TeaServer#soak(def)}からハンドラを作成したときに呼びます。<br/>
	 * 必要に応じてオーバーライドし、妥当性の検証や初期化に利用してください。
	 * @see #validateBasic()
	 * @throws TeaMakerMakeException 妥当性の検証で問題がみつかりました。
	 */
	void validate(){
		// なにもしません
	}
	
	/**
	 * このハンドラならびに下位ハンドラの妥当性を検証します。
	 * @see #validate()
	 * @throws TeaMakerMakeException 妥当性の検証で問題がみつかりました。
	 */
	void validateBasic(){
		// スカラー値をチェックするクロージャです
		Closure checkScalar = { def val ->
			try {
				TpacScalar.format(val);
			} catch (IllegalArgumentException exc){
				throw new TeaMakerMakeException("スカラー値が不正です。val=${val}");
			}
		}
		// コレクションを再帰的にチェックするクロージャです
		Closure checkCollec;
		checkCollec = { def target ->
			switch (target){
				case TpacText: break;
				case List:
					target.each { def elem -> (elem instanceof List || elem instanceof Map)? checkCollec(elem) : checkScalar(elem) }
					break;
				case Map:
					target.each { String key, def elem ->
						if (!(key ==~ cnstTeaHandle.pattern.mapkey)) throw new TeaMakerMakeException("マップキーの値が不正です。key=${key}");
						(elem instanceof List || elem instanceof Map)? checkCollec(elem) : checkScalar(elem);
					}
					break;
				default: checkScalar(target);
			}
		}
		// ハンドルが保持する値を検証します
		if (!(tag ==~ cnstTeaHandle.pattern.tag)) throw new TeaMakerMakeException("タグの値が不正です。tag=${tag}");
		if (!(name ==~ cnstTeaHandle.pattern.name)) throw new TeaMakerMakeException("名前の値が不正です。name=${name}");
		checkScalar(scalar);
		checkCollec(list);
		checkCollec(map);
		// 上記以外にチェックすべき妥当性があれば検証します
		validate();
		// 下位ハンドラの妥当性を検証します
		lowers.values().each { it.validateBasic() }
	}
	
	/**
	 * 識別キーを返します。<br>
	 * 識別キーはタグ名と名前を半角コロンで連結した文字列です。
	 * @return 識別キー
	 */
	String getKey(){
		return "${tag}${cnstTeaHandle.path.key}${name}" as String;
	}
	
	/**
	 * このハンドルの階層を返します。
	 * @return 階層
	 */
	int getLevel(){
		return upper.level + 1;
	}
	
	/**
	 * このハンドルのパスを返します。
	 * @return パス
	 * @throws IllegalStateException 上位ハンドルが設定されていません。
	 */
	String getPath(){
		return "${upper.path}${cnstTeaHandle.path.level}${key}" as String;
	}
	
	/**
	 * パスに対応するハンドルを返します。
	 * @param path パス
	 * @return パスに対応するハンドル
	 */
	TeaHandle path(String path){
		ArgmentChecker.checkNotNull('パス', path);
		def hndl;
		switch (path){
			case {it.empty}: // 自ハンドルの場合
				hndl = this;
				break;
			case {it.startsWith(cnstTeaHandle.path.level)}: // 絶対パスの場合
				hndl = upper.path(path);
				break;
			case {it.startsWith(cnstTeaHandle.path.upper)}: // 相対的に上位のパスの場合
				path = path.substring(cnstTeaHandle.path.upper.length());
				if (path.startsWith(cnstTeaHandle.path.level)) path = path.substring(cnstTeaHandle.path.level.length());
				hndl = upper.path(path);
				break;
			default: // 上記以外の場合、下位のハンドルを参照します
				if (cnstTeaHandle.path.handles.every { !(path ==~ it) }){
					throw new IllegalArgumentException("パスから参照先を特定できません。path=${path}");
				}
				Matcher matcher = Matcher.getLastMatcher();
				String key = matcher.group(1);
				String other = (matcher.groupCount() >= 2)? matcher.group(2) : null;
				hndl = (other == null)? lowers[key] : lowers[key]?.path(other);
				if (hndl == null) throw new IllegalArgumentException("パスから参照先を特定できません。path=${path}");
		}
		return hndl;
	}
	
	/**
	 * 識別キーが正規表現と一致する下位ハンドラのリストを取得します。
	 * @param regex 正規表現
	 * @return 識別キーが正規表現と一致する下位ハンドラのリスト
	 */
	List<TeaHandle> findAll(String regex){
		return lowers.values().findAll { it.key.matches(regex) };
	}
	
	/**
	 * 文字列表現を出力します。
	 * スカラー値が初期値（Null値）の場合は出力しません。
	 * リストやマップの要素であるスカラー値が初期値の場合は出力します。
	 * @param writer Writer
	 */
	void write(Writer writer){
		// 値の前に区切り文字を入れるためのクロージャです
		Closure addDiv = { String jdg, String str -> return (StringUtils.isEmpty(jdg))? '' : " ${str}" }
		// 宣言あるいはハンドルの階層を表す文字列を返すクロージャです
		Closure hndlLvl = { int lvl ->
			switch (lvl){
				case 0: return "${cnstTeaHandle.tostr.decLevel}";
				case 1..3: return "${cnstTeaHandle.tostr.handleLevel * lvl}";
				default: return "${lvl}${cnstTeaHandle.tostr.handleLevel}";
			}
		}
		// リストあるいはマップの文字列表現を出力するクロージャです
		Closure writeCollec;
		writeCollec = { def target, int lvl ->
			switch (target){
				case TpacText: target.write(writer); break;
				case List:
					target.each { def elem ->
						String div = (elem instanceof List || elem instanceof Map)? System.lineSeparator() : '';
						writer << String.format(cnstTeaHandle.tostr.listFormat, cnstTeaHandle.tostr.collecLevel * lvl, div);
						writeCollec(elem, lvl + 1);
					}
					break;
				case Map:
					target.each { String key, def elem ->
						String div = (elem instanceof List || elem instanceof Map)? System.lineSeparator() : ' ';
						writer << String.format(cnstTeaHandle.tostr.mapFormat, cnstTeaHandle.tostr.collecLevel * lvl, key, div);
						writeCollec(elem, lvl + 1);
					}
					break;
				default: writer << "${TpacScalar.format(target)}${System.lineSeparator()}";
			}
		}
		// コメントの文字列表現を出力するクロージャです
		Closure writeComments = { List cmnt ->
			cmnt.each { writer << String.format(cnstTeaHandle.tostr.commentFormat, it) }
		}
		// ハンドル開始行
		writer << String.format(cnstTeaHandle.tostr.handleFormat, hndlLvl(level), tag, addDiv(name, name), addDiv(scalar, TpacScalar.format(scalar)));
		writeComments(comment.handle);
		// リスト
		if (list != null) writeCollec(list, 0);
		writeComments(comment.list);
		// マップ
		if (map != null) writeCollec(map, 0);
		writeComments(comment.map);
		// テキスト
		text.write(writer);
		writeComments(comment.text);
		// 下位のハンドル
		lowers.values().each { it.write(writer) }
	}
	
	/**
	 * このハンドラを指定されたハンドラで上書きします。</p>
	 * <ul>
	 * <li>タグ名、名前が一致するハンドラは保持する値を上書きします。</li>
	 * <li>タグ名、名前が一致しないハンドラを追加します。</li>
	 * <li>下位ハンドラも再帰的に同じ処理をします。</li>
	 * </ul>
	 * <p>マップは、一致するキーがあれば上書き、なければ新規追加となります。<br/>
	 * リストは、同じインデックスがあれば上書き、サイズが大きければ追加となります。<br/>
	 * ネストされたリストやマップの個別の上書きはしません。<br/>
	 * スカラー値、テキストは値が存在すれば上書きします。<br/>
	 * コメントは上書きをしません。
	 * @param otherHndl 上書きするハンドラ
	 * @return 上書き後のハンドラ
	 */
	TeaHandle blend(TeaHandle otherHndl){
		ArgmentChecker.checkClass('上書きするハンドラ', otherHndl, this.class);
		otherHndl.lowers.each { String key, TeaHandle otherLower ->
			if (lowers.containsKey(key)){
				lowers[key].blend(otherLower);
			} else {
				this << otherLower;
			}
		}
		if (otherHndl.scalar != null) scalar = otherHndl.scalar;
		if (!otherHndl.text.empty) text = otherHndl.text;
		otherHndl.list.eachWithIndex { def elem, int idx -> list[idx] = elem }
		map.putAll(otherHndl.map);
		return this;
	}
}
