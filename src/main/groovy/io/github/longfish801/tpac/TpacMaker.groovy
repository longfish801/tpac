/*
 * TpacMaker.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac;

import groovy.util.logging.Slf4j;
import io.github.longfish801.tpac.element.TpacChild;
import io.github.longfish801.tpac.element.TpacDeclaration;
import io.github.longfish801.tpac.element.TpacElement;
import io.github.longfish801.tpac.element.TpacParent;

/**
 * TPAC記法の文字列の解析にともない、各要素を生成します。<br>
 * 解析は io.github.longfish801.tpac.parser.TpacParserで実施します。<br>
 * TPAC文書のインスタンスを生成したい場合は{@link Tpac}を参照してください。
 * @version 1.0.00 2017/07/04
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacMaker implements TpacMakerIF {
	/** TpacDeclaration */
	TpacDeclaration dec = null;
	
	/** {@inheritDoc} */
	void createRoot(String tag, String name, int lineNo){
		dec = new TpacDeclaration(tag, name);
		dec.lineNo = lineNo;
	}
	
	/** {@inheritDoc} */
	void createParent(String tag, String name, int lineNo){
		TpacParent parent = new TpacParent(tag, name);
		parent.lineNo = lineNo;
		dec << parent;
	}
	
	/** {@inheritDoc} */
	void createChild(String tag, String name, int lineNo){
		TpacChild child = new TpacChild(tag, name);
		child.lineNo = lineNo;
		TpacParent parent = dec.lowers.values().last();
		dec.lowers.values().last() << child;
	}
	
	/** {@inheritDoc} */
	void createAttr(String key, String value, int lineNo){
		currentElem().attrs[key] = value;
	}
	
	/** {@inheritDoc} */
	void createText(String text, int lineNo){
		currentElem().text = text;
	}
	
	/** {@inheritDoc} */
	Object getResult(){
		return dec;
	}
	
	/**
	 * 現在解析の対象となっている要素を返します。
	 * @return 現在解析の対象となっている要素
	 */
	TpacElement currentElem(){
		if (dec.lowers.values().size() == 0) return dec;
		TpacParent parent = dec.lowers.values().last();
		return (parent.lowers.size() == 0)? parent : parent.lowers.values().last();
	}
}
