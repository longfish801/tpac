/*
 * TpacHandleSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.util.logging.Slf4j
import io.github.longfish801.tpac.TpacMsg as msgs
import java.util.regex.Pattern
import spock.lang.Specification
import spock.lang.Unroll

/**
 * TpacHandleクラスのテスト。
 * @version 0.3.00 2020/06/03
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacHandleSpec extends Specification {
	def 'getKey'(){
		given:
		TpacHandle handle
		
		when:
		handle = new TpacHandle(tag: 'some', name: 'handle')
		then:
		handle.key == 'some:handle'
		
		when: '名前が空文字の場合はタグ名のみを返します'
		handle = new TpacHandle(tag: 'some', name: '')
		then:
		handle.key == 'some'
	}
	
	def 'getDec'(){
		given:
		TpacDec dec
		TpacHandle handle
		
		when:
		dec = new TpacDec(tag: 'some', name: 'dec')
		handle = new TpacHandle(tag: 'some', name: 'handle')
		dec << handle
		then:
		handle.dec == dec
	}
	
	def 'getLevel'(){
		given:
		TpacDec dec
		TpacHandle handle
		
		when:
		dec = new TpacDec(tag: 'some', name: 'dec')
		handle = new TpacHandle(tag: 'some', name: 'handle')
		dec << handle
		then:
		handle.level == 1
	}
	
	def 'leftShift'(){
		given:
		TpacHandle handle = new TpacHandle(tag: 'handle')
		TpacHandle lower = new TpacHandle(tag: 'lower')
		
		when:
		handle << lower
		then:
		handle.lowers['lower'] == lower
		lower.upper == handle
	}
	
	def 'getAt/setAt'(){
		given:
		TpacHandle handle
		
		when:
		handle = new TpacHandle(tag: 'some', name: 'handle')
		handle.setAt('key', 'foo')
		then:
		handle['key'] == 'foo'
	}
	
	def 'propertyMissing'(){
		given:
		TpacHandle handle
		
		when:
		handle = new TpacHandle(tag: 'some', name: 'handle')
		handle['boo'] = 'foo'
		then:
		handle['boo'] == 'foo'
	}
	
	def 'getPath'(){
		given:
		TpacDec dec
		TpacHandle handle
		
		when:
		dec = new TpacDec(tag: 'some', name: 'dec')
		handle = new TpacHandle(tag: 'some', name: 'handle')
		dec << handle
		then:
		handle.path == '/some:dec/some:handle'
	}
	
	@Unroll
	def 'solvePath'(){
		given:
		TpacServer server = new TpacServer()
		TpacDec dec = new TpacDec(tag: 'some', name: 'dec')
		TpacHandle handle = new TpacHandle(tag: 'some', name: 'handle')
		TpacHandle lower = new TpacHandle(tag: 'some', name: 'lower')
		TpacHandle lowerlower = new TpacHandle(tag: 'some', name: 'lowerlower')
		server << dec
		dec << handle
		handle << lower
		lower << lowerlower
		
		expect:
		handle.solvePath(path).path == expect
		
		where:
		path							|| expect
		'/some:dec/some:handle'			|| '/some:dec/some:handle'
		'..'							|| '/some:dec'
		'../some:handle'				|| '/some:dec/some:handle'
		'some:lower'					|| '/some:dec/some:handle/some:lower'
		'some:lower/some:lowerlower'	|| '/some:dec/some:handle/some:lower/some:lowerlower'
	}
	
	def 'solvePath - exception'(){
		given:
		TpacHandle handle = new TpacHandle(tag: 'some', name: 'handle')
		TpacHandlingException exc
		
		when:
		handle.solvePath('#')
		then:
		exc = thrown(TpacHandlingException)
		exc.message == String.format(msgs.exc.invalidpath, '#')
	}
	
	def 'findAll'(){
		given:
		TpacHandle handle = new TpacHandle(tag: 'some', name: 'handle')
		TpacHandle lower1 = new TpacHandle(tag: 'some')
		TpacHandle lower2 = new TpacHandle(tag: 'some', name: 'lower2')
		handle << lower1
		handle << lower2
		List list
		
		when: 'タグが someで、名前が空文字ではない下位ハンドルを取得します'
		list = handle.findAll(/^some:/)
		then:
		list.size() == 1
		list.collect { it.key } == [ 'some:lower2' ]
		
		when: 'タグが someの下位ハンドルを取得します'
		list = handle.findAll(/^some|some:.+$/)
		then:
		list.size() == 2
		list.collect { it.key } == [ 'some', 'some:lower2' ]
		
		when: 'みつからない場合は空リストを返します'
		list = handle.findAll(/nosuch/)
		then:
		list.size() == 0
	}
	
	def 'write'(){
		given:
		Closure getString = { TpacHandle hndl ->
			StringWriter writer = new StringWriter()
			hndl.write(writer)
			return writer.toString()
		}
		TpacDec dec
		TpacHandle handle
		TpacHandle lower
		TpacHandle lowerlower
		TpacHandle lowerlowerlower
		String result
		String expected
		
		when: '最小限の設定'
		dec = new TpacDec(tag: 'dec')
		handle = new TpacHandle(tag: 'handle')
		dec << handle
		result = getString(handle)
		expected = '''\
			#> handle
			#>
			
			'''.stripIndent().denormalize()
		then:
		result == expected
		
		when: 'デフォルトキーにテキスト、マップ（スカラー値、テキスト）を設定'
		dec = new TpacDec(tag: 'dec')
		handle = new TpacHandle(tag: 'handle', name: 'hello')
		dec << handle
		handle.comments << 'comment 1'
		handle.comments << 'comment 2'
		handle.comments << 'comment 3'
		handle['_'] = [ 'default value1', 'default value2' ]
		handle['key1'] = 'val1'
		handle['key2'] = [ 'val2', 'val3' ]
		result = getString(handle)
		expected = '''\
			#> handle:hello
			#:comment 1
			#:comment 2
			#:comment 3
			#-key1 val1
			default value1
			default value2
			#-key2
			val2
			val3
			#>
			
			'''.stripIndent().denormalize()
		then:
		result == expected
		
		when: '下位ハンドル'
		dec = new TpacDec(tag: 'dec')
		handle = new TpacHandle(tag: 'handle')
		lower = new TpacHandle(tag: 'lower')
		lowerlower = new TpacHandle(tag: 'lowerlower')
		lowerlowerlower = new TpacHandle(tag: 'lowerlowerlower')
		dec << handle
		handle << lower
		lower << lowerlower
		lowerlower << lowerlowerlower
		result = getString(handle)
		expected = '''\
			#> handle
			#>
			
			#>> lower
			#>
			
			#>>> lowerlower
			#>
			
			#4> lowerlowerlower
			#>
			
			'''.stripIndent().denormalize()
		then:
		result == expected
	}
	
	def 'formatText'(){
		given:
		List lines
		String result
		String expected
		
		when: '先頭が半角シャープで始まる行がなければ範囲を示す区切り行は省略します'
		lines = [ 'a', 'b', 'c' ]
		result = TpacHandle.formatText(lines)
		expected = '''\
			a
			b
			c'''.stripIndent().denormalize()
		then:
		result == expected
		
		when: '範囲を示す区切り行で挟んだ文字列表現を返します'
		lines = [ 'a', '#b', 'c' ]
		result = TpacHandle.formatText(lines)
		expected = '''\
			#===
			a
			#b
			c
			#==='''.stripIndent().denormalize()
		then:
		result == expected
		
		when: '使用可能な区切り行を決定します'
		lines = [ 'a', '#===', 'c' ]
		result = TpacHandle.formatText(lines)
		expected = '''\
			#====
			a
			#===
			c
			#===='''.stripIndent().denormalize()
		then:
		result == expected
	}
	
	@Unroll
	def 'formatScalar'(){
		expect:
		TpacHandle.formatScalar(value) == expect
		
		where:
		value		|| expect
		null		|| 'null'
		true		|| 'true'
		false		|| 'false'
		-1			|| '-1'
		2.3			|| '2.3'
		TpacRefer.newInstance(new TpacHandle(tag: 'handle'), '..')	|| '@..'
		Pattern.compile(/.+/)	|| ':.+'
		new TpacEval('3+2')	|| '=3+2'
		'null'		|| '_null'
		'true'		|| '_true'
		'false'		|| '_false'
		'@..'		|| '_@..'
		':.+'		|| '_:.+'
		'=3+2'		|| '_=3+2'
		'_hello'	|| '__hello'
		"a\nb"		|| '_a\\nb'
		''			|| '_'
		'abc'		|| 'abc'
	}
	
	def 'formatScalar - exception'(){
		given:
		TpacHandlingException exc
		
		when:
		TpacHandle.formatScalar([])
		then:
		exc = thrown(TpacHandlingException)
		exc.message == String.format(msgs.exc.noSupportScalarString, '[]', 'java.util.ArrayList')
	}
	
	def 'clone'(){
		given:
		TpacDec dec = new TpacDec(tag: 'dec')
		TpacHandle handle = new TpacHandle(tag: 'handle')
		TpacHandle lower = new TpacHandle(tag: 'lower')
		TpacHandle cloned
		
		when:
		dec << handle
		handle << lower
		handle.comments << 'Comment'
		handle['KEY'] = 'VAL'
		cloned = handle.clone()
		then:
		cloned.upper.key == dec.key
		cloned.lowers['lower'].key == lower.key
		cloned.comments[0] == 'Comment'
		cloned.KEY == 'VAL'
	}
	
	def 'plus'(){
		given:
		TpacDec dec1 = new TpacDec(tag: 'dec1')
		TpacDec dec2 = new TpacDec(tag: 'dec2')
		TpacHandle handle1 = new TpacHandle(tag: 'handle1')
		TpacHandle lower11 = new TpacHandle(tag: 'lower1')
		TpacHandle lower12 = new TpacHandle(tag: 'lower2')
		TpacHandle lowerlower111 = new TpacHandle(tag: 'lowerlower1')
		TpacHandle lowerlower121 = new TpacHandle(tag: 'lowerlower2')
		TpacHandle handle2 = new TpacHandle(tag: 'handle2')
		TpacHandle lower22 = new TpacHandle(tag: 'lower2')
		TpacHandle lower23 = new TpacHandle(tag: 'lower3')
		TpacHandle lowerlower221 = new TpacHandle(tag: 'lowerlower2')
		TpacHandle lowerlower231 = new TpacHandle(tag: 'lowerlower3')
		TpacHandle merged
		StringWriter writer
		String result
		String expected
		
		when:
		// handle1
		dec1 << handle1
		handle1 << lower11
		handle1 << lower12
		lower11 << lowerlower111
		lower12 << lowerlower121
		handle1['key1'] = 'val1-1'
		handle1['key2'] = 'val1-2'
		lower11['key1'] = 'val11'
		lower12['key1'] = 'val12-1'
		lower12['key2'] = 'val12-2'
		lowerlower111['key111'] = 'val111'
		lowerlower121['key1'] = 'val121-1'
		lowerlower121['key2'] = 'val121-2'
		// handle2
		dec2 << handle2
		handle2 << lower22
		handle2 << lower23
		lower22 << lowerlower221
		lower23 << lowerlower231
		handle2['key2'] = 'val2-2'
		handle2['key3'] = 'val2-3'
		lower22['key2'] = 'val22-2'
		lower22['key3'] = 'val22-3'
		lower23['key1'] = 'val23'
		lowerlower221['key2'] = 'val221-2'
		lowerlower221['key3'] = 'val221-3'
		lowerlower231['key231'] = 'val231'
		// マージします
		merged = handle1 + handle2
		writer = new StringWriter()
		merged.write(writer)
		result = writer.toString()
		expected = '''\
			#> handle1
			#-key1 val1-1
			#-key2 val2-2
			#-key3 val2-3
			#>
			
			#>> lower1
			#-key1 val11
			#>
			
			#>>> lowerlower1
			#-key111 val111
			#>
			
			#>> lower2
			#-key1 val12-1
			#-key2 val22-2
			#-key3 val22-3
			#>
			
			#>>> lowerlower2
			#-key1 val121-1
			#-key2 val221-2
			#-key3 val221-3
			#>
			
			#>> lower3
			#-key1 val23
			#>
			
			#>>> lowerlower3
			#-key231 val231
			#>
			
			'''.stripIndent().denormalize()
		then:
		result == expected
	}
}
