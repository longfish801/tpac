/*
 * TpacScalar.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.ArgmentChecker;
import org.apache.commons.text.StringEscapeUtils;

/**
 * tpac文書のハンドラです。
 * @version 1.0.00 2018/08/10
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacScalar {
	/**
	 * 文字列を評価してスカラー値を返します。
	 * @param raw 評価対象の文字列
	 * @param handle ハンドル
	 * @return スカラー値
	 */
	static def eval(String raw, TeaHandle handle){
		ArgmentChecker.checkNotNull('スカラー値', raw);
		ArgmentChecker.checkNotNull('ハンドル', handle);
		def value;
		switch (raw){
			case TpacHandle.cnstTeaHandle.scalar.kwdNull:
				value = null;
				break;
			case TpacHandle.cnstTeaHandle.scalar.kwdTrue:
				value = true;
				break;
			case TpacHandle.cnstTeaHandle.scalar.kwdFalse:
				value = false;
				break;
			case {it ==~ TpacHandle.cnstTeaHandle.scalar.numInt}:
				value = Integer.parseInt(raw);
				break;
			case {it ==~ TpacHandle.cnstTeaHandle.scalar.numBigDecimal}:
				value = new BigDecimal(raw);
				break;
			case {it.startsWith(TpacHandle.cnstTeaHandle.scalar.refer)}:
				value = TpacRefer.newInstance(raw.substring(TpacHandle.cnstTeaHandle.scalar.refer.length()), handle);
				break;
			case {it.startsWith(TpacHandle.cnstTeaHandle.scalar.str)}:
				value = StringEscapeUtils.unescapeJava(raw.substring(TpacHandle.cnstTeaHandle.scalar.str.length()));
				break;
			default:
				value = raw;
		}
		return value;
	}
	/**
	 * スカラー値を tpac文書の文字列に変換します。<br/>
	 * 他のデータ型に変換される恐れのある文字列は、明示的に文字列を意味する文字列表現に変換します。
	 * @param value スカラー値
	 * @return tpac文書の文字列
	 */
	static String format(def value){
		String raw;
		switch (value){
			case null:
				raw = 'null';
				break;
			case true:
				raw = 'true';
				break;
			case false:
				raw = 'false';
				break;
			case Integer:
			case BigDecimal:
				raw = value.toString();
				break;
			case TpacRefer:
				raw = "${TpacHandle.cnstTeaHandle.scalar.refer}${value.toString()}";
				break;
			case String:
				switch (value){
					case TpacHandle.cnstTeaHandle.scalar.kwdNull:
					case TpacHandle.cnstTeaHandle.scalar.kwdTrue:
					case TpacHandle.cnstTeaHandle.scalar.kwdFalse:
					case {it ==~ TpacHandle.cnstTeaHandle.scalar.numInt}:
					case {it ==~ TpacHandle.cnstTeaHandle.scalar.numBigDecimal}:
					case {it.startsWith(TpacHandle.cnstTeaHandle.scalar.refer)}:
					case {it.startsWith(TpacHandle.cnstTeaHandle.scalar.str)}:
					case {it ==~ /.*[\r\n]..*/}:
					case {it.empty}:
						value = StringEscapeUtils.escapeJava(value);
						raw = "${TpacHandle.cnstTeaHandle.scalar.str}${value}";
						break;
					default:
						raw = value.toString();
						break;
				}
				break;
			default:
				throw new IllegalArgumentException("文字列表現に変換できないスカラー値です。value=${value} class=${value.class.name}");
		}
		return raw;
	}
}
