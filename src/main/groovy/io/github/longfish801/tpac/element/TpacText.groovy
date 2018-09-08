/*
 * TpacText.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.ArgmentChecker;

/**
 * tpac文書のテキストです。
 * @version 1.0.00 2018/08/10
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacText extends ArrayList {
	/**
	 * 各行の末尾に改行を付与して連結したテキストを返します。<br/>
	 * テキストが未格納であれば空文字を返します。
	 * @return 各行の末尾に改行を付与して連結したテキスト
	 */
	String toString(){
		return (this.empty)? '' : this.collect {"${it}${System.lineSeparator()}" }.join();
	}
	
	/**
	 * 文字列表現を出力します。
	 * @param writer Writer
	 */
	void write(Writer writer){
		ArgmentChecker.checkNotNull('Writer', writer);
		this.each {
			writer << (
				(it ==~ TpacHandle.cnstTeaHandle.tostr.textMeta)?
				"${TpacHandle.cnstTeaHandle.tostr.textEscape}${it}${System.lineSeparator()}" : 
				"${it}${System.lineSeparator()}"
			)
		}
	}
}
