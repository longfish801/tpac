/*
 * TpacMakerIF.java
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac;

/**
 * TPAC文書の変換に用いるインタフェースです。
 * @version 1.0.00 2016/03/11
 * @author io.github.longfish801
 */
public interface TpacMakerIF {
	/**
	 * ルート要素を作成します。
	 * @param tag タグ名
	 * @param name 名前
	 * @param lineNo 行番号
	 */
	void createRoot(String tag, String name, int lineNo);
	
	/**
	 * 親要素を作成します。
	 * @param tag タグ名
	 * @param name 名前
	 * @param lineNo 行番号
	 */
	void createParent(String tag, String name, int lineNo);
	
	/**
	 * 子要素を作成します。
	 * @param tag タグ名
	 * @param name 名前
	 * @param lineNo 行番号
	 */
	void createChild(String tag, String name, int lineNo);
	
	/**
	 * 属性を作成します。
	 * @param key 属性名
	 * @param value 属性値
	 * @param lineNo 行番号
	 */
	void createAttr(String key, String value, int lineNo);
	
	/**
	 * テキスト要素を作成します。
	 * @param text テキスト
	 * @param lineNo 行番号
	 */
	void createText(String text, int lineNo);
	
	/**
	 * TPAC文書の変換結果を返します。
	 * @return TPAC文書の変換結果
	 */
	Object getResult();
}
