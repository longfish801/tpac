/*
 * TpacElement.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import groovy.xml.MarkupBuilder;
import io.github.longfish801.shared.lang.ArgmentChecker;
import io.github.longfish801.shared.util.ClassSlurper;

/**
 * TPAC文書の要素です。
 * @version 1.0.00 2017/06/30
 * @author io.github.longfish801
 */
@Slf4j('LOG')
abstract class TpacElement {
	/** ConfigObject */
	protected static final ConfigObject constants = ClassSlurper.getConfig(TpacElement.class);
	/** 文字列表現時の先頭記号 */
	String sign = null;
	/** 行番号 */
	int lineNo = -1;
	/** タグ */
	String tag = '';
	/** 名前 */
	String name = '';
	/** 属性一覧 */
	TpacAttrs<String, String> attrs = new TpacAttrs<String, String>();
	/** テキスト */
	String text = '';
	/** 識別キーと下位要素とのマップ */
	Map<String, TpacElement> lowers = [:];
	/** 上位要素 */
	TpacElement upper = null;
	/** 下位要素として可能なクラス */
	Class lowerClass = null;
	
	/**
	 * コンストラクタ。
	 * @param tag タグ
	 * @param name 名前
	 */
	TpacElement(String tag, String name) {
		setTag(tag);
		setName(name);
	}
	
	/**
	 * タグを設定します。
	 * @param tag タグ
	 */
	void setTag(String tag){
		ArgmentChecker.checkMatchRex('タグ', tag, /[^\r\n #]+/);
		this.tag = tag;
	}
	
	/**
	 * 名前を設定します。
	 * @param name 名前
	 */
	void setName(String name){
		ArgmentChecker.checkMatchRex('名前', name, /[^\r\n #]*/);
		this.name = name;
	}
	
	/**
	 * テキストを設定します。
	 * @param text テキスト
	 */
	void setText(String text){
		ArgmentChecker.checkNotNull('テキスト', text);
		this.text = text;
	}
	
	/**
	 * 識別キーを返します。<br>
	 * 識別キーはタグ名と名前を半角シャープで連結した文字列です。
	 * @return 識別キー
	 */
	String getKey(){
		return "${tag}#${name}" as String;
	}
	
	/**
	 * 下位要素を追加します。
	 * @param lower 下位要素
	 * @return 自インスタンス
	 */
	TpacElement leftShift(TpacElement lower){
		ArgmentChecker.checkUniqueKey('下位要素', lower.key, lowers);
		ArgmentChecker.checkClass('下位要素', lower, getLowerClass());
		lowers[lower.key] = lower;
		lower.upper = this;
		return this;
	}
	
	/**
	 * 識別キーが正規表現と一致する下位要素のリストを取得します。
	 * @param regex 正規表現
	 * @return 識別キーが正規表現と一致する下位要素のリスト
	 */
	List<TpacElement> findAll(String regex){
		return lowers.values().findAll { it.key.matches(regex) };
	}
	
	/**
	 * 下位要素として可能なクラスを返します。
	 * @return 下位要素として可能なクラス
	 */
	abstract Class getLowerClass();
	
	/**
	 * 文字列表現時の先頭記号を返します。
	 * @return 文字列表現時の先頭記号
	 */
	abstract String getSign();
	
	/**
	 * 文字列表現を返します。
	 * @return 文字列
	 */
	String toString(){
		StringBuilder builder = StringBuilder.newInstance();
		builder << getSign();
		builder << tag;
		if (!name.empty){
			builder << ' ';
			builder << name;
		}
		builder << "\n";
		builder << attrs.toString();
		if (!text.empty) builder << text;
		lowers.values().each { builder << it.toString() }
		return builder.toString();
	}
	
	/**
	 * このインスタンスをXML形式で表現した文字列をMarkupBuilderで生成します。
	 * @param builder MarkupBuilder
	 */
	void writeXml(MarkupBuilder builder){
		builder."${tag}"(name: name, *:(attrs), (text.empty)? null : text){
			lowers.values().each { TpacElement lower -> lower.writeXml(builder) }
		}
	}
	
	/**
	 * この要素を指定された要素で上書きします。<br>
	 * 宣言、親要素、子要素同士でしか上書きできません。</p>
	 * <ul>
	 * <li>タグ名、名前が一致する要素は属性値、テキストを上書きします。</li>
	 * <li>タグ名、名前が一致しない要素を追加します。</li>
	 * <li>下位要素も再帰的に同じ処理をします。</li>
	 * </ul>
	 * <p>属性の上書きは、一致する属性名があれば上書き、なければ新規追加となります。
	 * @param tpac 上書きする要素
	 * @return 上書き後の要素
	 */
	TpacElement blend(TpacElement otherElem){
		ArgmentChecker.checkClass('上書きする要素', otherElem, this.class);
		otherElem.lowers.each { String key, TpacElement otherLower ->
			if (lowers.containsKey(key)){
				lowers[key].blend(otherLower);
			} else {
				this << otherLower;
			}
		}
		attrs.putAll(otherElem.attrs);
		text = otherElem.text;
		return this;
	}
}
