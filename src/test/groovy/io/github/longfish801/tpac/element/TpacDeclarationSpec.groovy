/*
 * TpacDeclarationSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.PackageDirectory;
import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * TpacDeclarationクラスのテスト。
 * @version 1.0.00 2017/07/04
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacDeclarationSpec extends Specification {
	/** ファイル入出力のテスト用フォルダ */
	private static final File testDir = PackageDirectory.deepDir('src/test/resources', TpacDeclarationSpec.class);
	
	def 'コンストラクタ'(){
		when:
		new TpacDeclaration('tag', 'name');
		then:
		noExceptionThrown();
		
		when:
		new TpacDeclaration('', 'name');
		then:
		thrown(IllegalArgumentException);
		
		when:
		new TpacDeclaration('tag', '');
		then:
		noExceptionThrown();
		
		when:
		new TpacDeclaration('tag', null);
		then:
		thrown(IllegalArgumentException);
	}
	
	def 'テキストを設定します'(){
		given:
		TpacDeclaration tpac = null;
		
		when:
		tpac = new TpacDeclaration('tag', 'name');
		tpac.text = '';
		then:
		noExceptionThrown();
		
		when:
		tpac = new TpacDeclaration('tag', 'name');
		tpac.text = null;
		then:
		thrown(IllegalArgumentException);
	}
	
	def '識別キーを返します'(){
		given:
		TpacDeclaration tpac = null;
		
		when:
		tpac = new TpacDeclaration('tag', 'name');
		then:
		tpac.key == 'tag#name';
		
		when:
		tpac = new TpacDeclaration('tag', '');
		then:
		tpac.key == 'tag#';
	}
	
	def '下位要素を追加します'(){
		given:
		TpacDeclaration tpac = null;
		TpacParent parent = null;
		TpacChild child = null;
		
		when:
		tpac = new TpacDeclaration('root', 'name');
		parent = new TpacParent('parent', 'name');
		tpac << parent;
		then:
		tpac.lowers['parent#name'] == parent;
		parent.upper.key == 'root#name';
		
		when:
		tpac = new TpacDeclaration('root', 'name');
		child = new TpacChild('child', 'name');
		tpac << child;
		then:
		thrown(IllegalArgumentException);
		
		when:
		tpac = new TpacDeclaration('root', 'name');
		tpac << new TpacParent('parent', 'name');
		tpac << new TpacParent('parent', 'name');
		then:
		thrown(IllegalArgumentException);
	}
	
	def '識別キーが正規表現と一致する下位要素のリストを取得します'(){
		given:
		TpacDeclaration tpac = null;
		tpac = new TpacDeclaration('root', 'name');
		tpac << new TpacParent('tagA', 'name1');
		tpac << new TpacParent('tagB', 'name1');
		tpac << new TpacParent('tagA', 'name2');
		tpac << new TpacParent('tagC', 'name3');
		tpac << new TpacParent('tagB', 'name2');
		tpac << new TpacParent('tagA', 'name3');
		List list = null;
		
		when:
		list = tpac.findAll(/tagA#.+/);
		then:
		list.collect { it.key } == [ 'tagA#name1', 'tagA#name2', 'tagA#name3' ];
		
		when:
		list = tpac.findAll(/.+#name2/);
		then:
		list.collect { it.key } == [ 'tagA#name2', 'tagB#name2' ];
	}
	
	def '文字列表現を返します'(){
		given:
		TpacDeclaration tpac = null;
		TpacParent parent = null;
		TpacChild child = null;
		
		when:
		tpac = new TpacDeclaration('tag', 'name');
		then:
		tpac.toString() == "#! tag name\n";
		
		when:
		tpac = new TpacDeclaration('tag', '');
		then:
		tpac.toString() == "#! tag\n";
		
		when:
		tpac = new TpacDeclaration('root', 'name');
		parent = new TpacParent('parent', 'name');
		child = new TpacChild('child', 'name');
		tpac << parent;
		parent << child;
		then:
		tpac.toString() == "#! root name\n## parent name\n# child name\n";
	}
}
