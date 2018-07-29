/*
 * Tpac.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac;

import groovy.transform.InheritConstructors;
import groovy.util.logging.Slf4j;
import groovy.xml.MarkupBuilder;
import io.github.longfish801.shared.lang.ArgmentChecker;
import io.github.longfish801.shared.util.ClassSlurper;
import io.github.longfish801.tpac.parser.TpacParser;
import io.github.longfish801.tpac.element.TpacDeclaration;
import io.github.longfish801.tpac.parser.ParseException;
import io.github.longfish801.tpac.parser.TokenMgrError;

/**
 * TPAC文書です。
 * @version 1.0.00 2017/07/04
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class Tpac {
	/** ConfigObject */
	protected static final ConfigObject constants = ClassSlurper.getConfig(Tpac.class);
	/** 宣言要素 */
	TpacDeclaration dec = null;
	
	/**
	 * 宣言要素からインスタンスを生成するコンストラクタです。
	 * @param dec 宣言要素
	 */
	Tpac(TpacDeclaration dec){
		this.dec = dec;
	}
	
	/**
	 * ファイル内容をTPAC文書とみなして解析結果を保持するコンストラクタです。
	 * @param file ファイル
	 * @exception TpacParseException TPAC文書の解析に失敗しました。
	 */
	Tpac(File file){
		dec = new FileInputStream(file).withCloseable { parse(it) };
	}
	
	/**
	 * URL先の内容をTPAC文書とみなして解析結果を保持するコンストラクタです。
	 * @param url URL
	 * @exception TpacParseException TPAC文書の解析に失敗しました。
	 */
	Tpac(URL url){
		dec = url.openStream().withCloseable { parse(it) };
	}
	
	/**
	 * 入力ストリームからTPAC記法の文字列を読みこみ、解析結果を保持するコンストラクタです。
	 * @param stream 入力ストリーム
	 * @exception TpacParseException TPAC文書の解析に失敗しました。
	 */
	Tpac(InputStream stream){
		dec = parse(stream);
	}
	
	/**
	 * TPAC記法の文字列を解析し結果を保持するコンストラクタです。
	 * @param text 文字列
	 * @exception TpacParseException TPAC文書の解析に失敗しました。
	 */
	Tpac(String text){
		dec = new StringReader(text).withCloseable { parse(it) };
	}
	
	/**
	 * リーダからTPAC記法の文字列を読みこみ、解析結果を保持するコンストラクタです。
	 * @param reader リーダ
	 * @exception TpacParseException TPAC文書の解析に失敗しました。
	 */
	Tpac(Reader reader){
		dec = parse(reader);
	}
	
	/**
	 * TPAC文書を解析するためのインスタンスを返します。<br>
	 * 本クラスを継承し、解析インスタンスを変更する場合は、本メソッドをオーバーライドしてください。
	 * @return TPAC文書を解析するためのインスタンス
	 */
	TpacMaker getMaker(){
		return new TpacMaker();
	}
	
	/**
	 * 指定された入力ストリームからTPAC記法の文字列を読みこみ、解析結果を返します。<br>
	 * 文字コードは環境変数file.encodingで指定された値を使用します。
	 * @param stream 入力ストリーム
	 * @return 宣言要素
	 * @exception TpacParseException TPAC文書の解析に失敗しました。
	 */
	protected TpacDeclaration parse(InputStream stream){
		TpacDeclaration dec = null;
		try {
			dec = new TpacParser(stream, System.getProperty('file.encoding')).parse(maker);
		} catch (TokenMgrError exc){
			throw new TpacParseException('字句誤りのためTPAC文書の解析に失敗しました。', exc);
		} catch (ParseException exc){
			throw new TpacParseException('構文誤りのためTPAC文書の解析に失敗しました。', exc);
		}
		return dec;
	}
	
	/**
	 * 指定されたリーダからTPAC記法の文字列を読みこみ、解析結果を返します。
	 * @param reader リーダ
	 * @return 宣言要素
	 * @exception TpacParseException TPAC文書の解析に失敗しました。
	 */
	protected TpacDeclaration parse(Reader reader){
		TpacDeclaration dec = null;
		try {
			dec = new TpacParser(reader).parse(maker);
		} catch (TokenMgrError exc){
			throw new TpacParseException('字句誤りのためTPAC文書の解析に失敗しました。', exc);
		} catch (ParseException exc){
			throw new TpacParseException('構文誤りのためTPAC文書の解析に失敗しました。', exc);
		}
		return dec;
	}
	
	/**
	 * 文字列表現を返します。
	 * @return 文字列
	 */
	String toString(){
		return dec.toString();
	}
	
	/**
	 * XML形式で表現した文字列を返します。
	 * @return XML形式で表現した文字列
	 */
	String toXml(){
		StringWriter writer = new StringWriter();
		MarkupBuilder builder = new MarkupBuilder(writer);
		builder.doubleQuotes = true;
		builder.mkp.xmlDeclaration(constants.xmlDec);
		dec.writeXml(builder);
		return writer.toString();
	}
	
	/**
	 * このTPAC文書を指定されたTPAC文書で上書きします。
	 * @param tpac 上書きするTPAC文書
	 * @return 上書き後のTPAC文書
	 * @see TpacElement#blend(TpacElement)
	 */
	Tpac blend(Tpac otherDoc){
		ArgmentChecker.checkClass('上書きする要素', otherDoc, Tpac.class);
		dec.blend(otherDoc.dec);
		return this;
	}
	
	/**
	 * TPAC文書の解析失敗を表す例外クラスです。
	 */
	@InheritConstructors
	class TpacParseException extends Exception { }
}
