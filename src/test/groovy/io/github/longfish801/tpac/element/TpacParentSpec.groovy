/*
 * TpacParentSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import spock.lang.Specification;

/**
 * TpacParentクラスのテスト。
 * @version 1.0.00 2017/07/04
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacParentSpec extends Specification {
	def '下位要素を追加します'(){
		given:
		TpacDeclaration dec = null;
		TpacParent parent = null;
		TpacChild child = null;
		
		when:
		parent = new TpacParent('parent', 'name');
		child = new TpacChild('child', 'name');
		parent << child;
		then:
		parent.lowers['child#name'] == child;
		child.upper.key == 'parent#name';
		
		when:
		parent = new TpacParent('parent', 'name');
		dec = new TpacDeclaration('root', 'name');
		parent << dec;
		then:
		thrown(IllegalArgumentException);
		
		when:
		parent = new TpacParent('parent', 'name');
		parent << new TpacChild('child', 'name');
		parent << new TpacChild('child', 'name');
		then:
		thrown(IllegalArgumentException);
	}
	
	def '文字列表現を返します'(){
		given:
		TpacParent parent = null;
		
		when:
		parent = new TpacParent('parent', 'name');
		then:
		parent.toString() == "## parent name\n";
		
		when:
		parent = new TpacParent('parent', '');
		then:
		parent.toString() == "## parent\n";
	}
}
