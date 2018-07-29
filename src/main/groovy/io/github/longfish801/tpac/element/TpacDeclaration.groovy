/*
 * TpacDeclaration.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.transform.InheritConstructors;
import groovy.util.logging.Slf4j;
import groovy.xml.MarkupBuilder;

/**
 * TPAC文書の宣言要素です。
 * @version 1.0.00 2017/07/07
 * @author io.github.longfish801
 */
@Slf4j('LOG')
@InheritConstructors
class TpacDeclaration extends TpacElement {
	/** 文字列表現時の先頭記号 */
	String sign = constants.SIGNDEC;
	/** 下位要素として可能なクラス */
	Class lowerClass = TpacParent.class;
}
