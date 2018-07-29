/*
 * TpacChildSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import spock.lang.Specification;

/**
 * TpacChildクラスのテスト。
 * @version 1.0.00 2017/07/04
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacChildSpec extends Specification {
	def '下位要素を追加します'(){
		given:
		TpacDeclaration dec = null;
		TpacParent parent = null;
		TpacChild child = null;
		
		when:
		child = new TpacChild('child', 'name');
		dec = new TpacDeclaration('root', 'name');
		child << dec;
		then:
		thrown(UnsupportedOperationException);
		
		when:
		child = new TpacChild('child', 'name');
		parent = new TpacParent('parent', 'name');
		child << parent;
		then:
		thrown(UnsupportedOperationException);
	}
	
	def '文字列表現を返します'(){
		given:
		TpacChild child = null;
		
		when:
		child = new TpacChild('child', 'name');
		then:
		child.toString() == "# child name\n";
		
		when:
		child = new TpacChild('child', '');
		then:
		child.toString() == "# child\n";
	}
}
