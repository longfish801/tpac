/*
 * TpacChild.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.transform.InheritConstructors;
import groovy.util.logging.Slf4j;

/**
 * TPAC文書の子要素です。
 * @version 1.0.00 2017/06/30
 * @author io.github.longfish801
 */
@Slf4j('LOG')
@InheritConstructors
class TpacChild extends TpacElement {
	/** 文字列表現時の先頭記号 */
	String sign = constants.SIGNCHLD;
	
	/** {@inheritDoc} */
	TpacElement leftShift(TpacElement lower){
		throw new UnsupportedOperationException();
	}
	
	/** {@inheritDoc} */
	Class getLowerClass(){
		throw new UnsupportedOperationException();
	}
}
