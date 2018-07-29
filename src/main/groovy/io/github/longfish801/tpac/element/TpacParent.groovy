/*
 * TpacParent.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.transform.InheritConstructors;
import groovy.util.logging.Slf4j;

/**
 * TPAC文書の親要素です。
 * @version 1.0.00 2017/06/30
 * @author io.github.longfish801
 */
@Slf4j('LOG')
@InheritConstructors
class TpacParent extends TpacElement {
	/** 文字列表現時の先頭記号 */
	String sign = constants.SIGNPRNT;
	/** 下位要素として可能なクラス */
	Class lowerClass = TpacChild.class;
}
