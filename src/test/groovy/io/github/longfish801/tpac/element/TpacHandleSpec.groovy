/*
 * TpacHandleSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.tpac.TeaServer;
import spock.lang.Specification;
import spock.lang.Unroll;
import spock.lang.Shared;

/**
 * TpacHandleクラスのテスト。
 * @version 1.0.00 2018/08/26
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacHandleSpec extends Specification {
	@Shared TeaDec dec;
	
	def setup(){
		dec = new TpacDec().setup('tpac', 'dec', new TeaServer());
	}
	
	def 'タグ、名前、上位ハンドルを格納します。'(){
		given:
		TeaHandle handle = new TpacHandle();
		
		when:
		handle.setup('some', 'thing', dec);
		then:
		handle.tag == 'some';
		handle.name == 'thing';
		handle.upper == dec;
		dec.lowers['some:thing'] == handle;
		
		when:
		handle.setup('some', '', dec);
		then:
		handle.name == '';
		
		when:
		handle.setup('some', 'thing', null);
		then:
		thrown(IllegalArgumentException);
	}
	
	def '下位ハンドラを追加します。'(){
		given:
		TeaHandle handle = new TpacHandle();
		handle.tag = 'some';
		handle.name = 'thing';
		
		when:
		dec << handle;
		then:
		handle.upper == dec;
		dec.lowers['some:thing'] == handle;
		
		when:
		dec << null;
		then:
		thrown(IllegalArgumentException);
		
		when:
		dec << handle;
		dec << handle;
		then:
		thrown(IllegalArgumentException);
	}
	
	def '識別キーを返します。'(){
		given:
		TeaHandle handle = new TpacHandle();
		handle.setup('some', 'thing', dec);
		
		expect:
		handle.key == 'some:thing';
	}
	
	def 'このハンドルの階層を返します。'(){
		given:
		TeaHandle handle = new TpacHandle();
		handle.setup('some', 'thing', dec);
		
		expect:
		handle.level == 1;
	}
	
	def 'このハンドルのパスを返します。'(){
		given:
		TeaHandle handle = new TpacHandle();
		handle.setup('some', 'thing', dec);
		
		expect:
		handle.path == '/tpac:dec/some:thing';
	}
	
	@Unroll
	def 'パスに対応するハンドルを返します。'(){
		given:
		TeaHandle handle = new TpacHandle();
		handle.setup('some', 'thing', dec);
		TeaHandle lower = new TpacHandle();
		lower.setup('every', 'where', handle);
		
		expect:
		handle.path(path).path == expect;
		
		where:
		path	|| expect
		''		|| '/tpac:dec/some:thing';
		'/tpac:dec/some:thing'	|| '/tpac:dec/some:thing';
		'..'	|| '/tpac:dec';
		'every:where'	|| '/tpac:dec/some:thing/every:where';
	}
	
	def '識別キーが正規表現と一致する下位ハンドラのリストを取得します。'(){
		given:
		TeaHandle handle = new TpacHandle();
		handle.setup('some', 'thing', dec);
		TeaHandle lower = new TpacHandle();
		lower.setup('every', 'where', handle);
		List list;
		
		when:
		list = handle.findAll(/every:.+/);
		then:
		list.first().path == '/tpac:dec/some:thing/every:where';
	}
	
	def '文字列表現を出力します。'(){
		given:
		TeaHandle handle = new TpacHandle();
		handle.setup('some', 'thing', dec);
		TeaHandle lower = new TpacHandle();
		lower.setup('every', 'where', handle);
		TeaHandle lower2 = new TpacHandle();
		lower2.setup('any', 'thing', lower);
		TeaHandle lower3 = new TpacHandle();
		lower3.setup('hello', 'world', lower2);
		dec.scalar = 'Groovy';
		dec.comment.handle << 'comment for handle';
		handle.text << 'Hello, World!';
		handle.text << '# Hello, Java!';
		handle.comment.text << 'comment for text';
		lower.list = [ 'Mon', [ 'AM', 'PM' ], 'TUE' ];
		lower.comment.list << 'comment for list';
		lower2.map = [ 'weight': 60.5, 'height' : [ 'now': 164 ]];
		lower2.comment.map << 'comment for map';
		lower3.list = [ 'Mon', [ 'now': 164 ], 'TUE', [ 'AM', 'PM' ] as TpacText ];
		
		StringWriter writer = new StringWriter();
		String result;
		String expect = '''\
			#! tpac dec Groovy
			# comment for handle
			#> some thing
			Hello, World!
				# Hello, Java!
			# comment for text
			#>> every where
			#_Mon
			#_
			#	_AM
			#	_PM
			#_TUE
			# comment for list
			#>>> any thing
			#-weight 60.5
			#-height
			#	-now 164
			# comment for map
			#4> hello world
			#_Mon
			#_
			#	-now 164
			#_TUE
			#_
			AM
			PM
			'''.stripIndent().replaceAll("\n", System.lineSeparator());
		
		when:
		dec.write(writer);
		result = writer.toString();
		then:
		result == expect;
	}
	
	def 'このハンドラを指定されたハンドラで上書きします。'(){
		given:
		TeaHandle handle1 = new TpacHandle();
		handle1.setup('some', 'thing', dec);
		handle1.scalar = 'Java';
		handle1.list = [ 'A', 'B' ];
		TeaHandle handle2 = new TpacHandle();
		handle2.setup('some', 'body', dec);
		handle2.scalar = 'Groovy';
		handle2.text << 'Hello!';
		handle1.list = [ 'a', 'b', 'c' ];
		
		TeaHandle lower11 = new TpacHandle();
		lower11.setup('every', 'thing', handle1);
		TeaHandle lower12 = new TpacHandle();
		lower12.setup('every', 'where', handle1);
		lower12.scalar = 'Python';
		lower12.map = [ 'a': 5, 'b': 8 ];
		
		TeaHandle lower21 = new TpacHandle();
		lower21.setup('every', 'where', handle2);
		lower21.map = [ 'b': 6, 'c': 9 ];
		
		TeaHandle lower22 = new TpacHandle();
		lower22.setup('every', 'body', handle2);
		
		StringWriter writer = new StringWriter();
		String result;
		String expect = '''\
			#> some thing Groovy
			#_a
			#_b
			#_c
			Hello!
			#>> every thing
			#>> every where Python
			#-a 5
			#-b 6
			#-c 9
			#>> every body
			'''.stripIndent().replaceAll("\n", System.lineSeparator());
		
		when:
		handle1.blend(handle2);
		handle1.write(writer);
		result = writer.toString();
		then:
		result == expect;
	}
}
