/*
 * TpacParseExceptions.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.transform.InheritConstructors

/**
 * tpac文書の解析エラーのリストを保持する例外クラスです。
 * @version 0.3.00 2020/04/30
 * @author io.github.longfish801
 */
@InheritConstructors
class TpacParseExceptions extends Exception {
	List errors = []
	
	/**
	 * ローカライズされたメッセージを返します。
	 * @return ローカライズされたメッセージ
	 */
	@Override
	String getLocalizedMessage(){
		List lines = []
		lines << message
		errors.each { ParseError err ->
			lines << "\tlineNo=${err.lineNo} line=${err.line}"
			lines << "\t\t${err.exc}"
		}
		return lines.join(System.lineSeparator())
	}
	
	/**
	 * 解析エラーを追加します。
	 * @param lineNo 行番号
	 * @param line 行文字列
	 * @param exc 例外
	 */
	void append(int lineNo, String line, Exception exc){
		errors << new ParseError(lineNo: lineNo, line: line, exc: exc)
	}
	
	/**
	 * 解析エラーです。
	 */
	class ParseError {
		int lineNo
		String line
		Exception exc
	}
}
