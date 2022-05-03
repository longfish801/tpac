/*
 * TpacHandleSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.util.logging.Slf4j
import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.TpacMsg as msgs
import io.github.longfish801.tpac.tea.TeaHandle
import java.util.regex.Pattern
import spock.lang.Specification
import spock.lang.Unroll

/**
 * TpacHandleクラスのテスト。
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
		
		when: '名前を省略した場合はタグ名のみを返します'
		handle = new TpacHandle(tag: 'some')
		then:
		handle.key == 'some'
	}
	
	def 'getKeyNatural'(){
		given:
		TpacHandle handle
		
		when:
		handle = new TpacHandle(tag: 'some', name: 'handle')
		then:
		handle.keyNatural == 'some:handle'
		
		when: '名前を省略した場合、名前として半角アンダーバーを用います'
		handle = new TpacHandle(tag: 'some')
		then:
		handle.keyNatural == 'some:dflt'
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
		handle.lowers['lower:dflt'] == lower
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
		
		when:
		dec = new TpacDec(tag: 'some')
		handle = new TpacHandle(tag: 'some')
		dec << handle
		then:
		handle.path == '/some/some'
	}
	
	@Unroll
	def 'solve'(){
		given:
		TpacServer server = new TpacServer()
		TpacDec dec = new TpacDec(tag: 'some', name: 'dec')
		TpacHandle handle = new TpacHandle(tag: 'some', name: 'handle')
		TpacHandle lower = new TpacHandle(tag: 'some', name: 'lower')
		TpacHandle lower2 = new TpacHandle(tag: 'some')
		TpacHandle lowerlower = new TpacHandle(tag: 'some', name: 'lowerlower')
		TpacHandle handle2 = new TpacHandle(tag: 'some', name: 'handle2')
		TpacHandle lower3 = new TpacHandle(tag: 'some', name: 'lower3')
		server << dec
		dec << handle
		handle << lower
		handle << lower2
		lower << lowerlower
		dec << handle2
		handle2 << lower3
		
		expect:
		handle.solve(path).path == expect
		
		where:
		path							|| expect
		'/some:dec/some:handle'			|| '/some:dec/some:handle'
		'..'							|| '/some:dec'
		'../some:handle'				|| '/some:dec/some:handle'
		'../some:handle/some:lower'		|| '/some:dec/some:handle/some:lower'
		'../some:handle2/some:lower3'	|| '/some:dec/some:handle2/some:lower3'
		'../some:handle2/..'			|| '/some:dec'
		'some:lower'					|| '/some:dec/some:handle/some:lower'
		'some'							|| '/some:dec/some:handle/some'
		'some:dflt'						|| '/some:dec/some:handle/some'
		'some:lower/some:lowerlower'	|| '/some:dec/some:handle/some:lower/some:lowerlower'
	}
	
	def 'solve - exception'(){
		given:
		TpacHandle handle = new TpacHandle(tag: 'some', name: 'handle')
		TpacHandlingException exc
		
		when:
		handle.solve('#')
		then:
		exc = thrown(TpacHandlingException)
		exc.message == String.format(msgs.exc.invalidpath, '#')
	}
	
	@Unroll
	def 'refer'(){
		given:
		TpacServer server = new TpacServer()
		TpacDec dec = new TpacDec(tag: 'some', name: 'dec')
		TpacHandle handle = new TpacHandle(tag: 'some', name: 'handle')
		TpacHandle lower = new TpacHandle(tag: 'some', name: 'lower')
		TpacHandle lower2 = new TpacHandle(tag: 'some')
		server << dec
		dec << handle
		handle << lower
		handle << lower2
		lower2.dflt = 'lower2dflt'
		lower2.somekey = 'lower2somekey'
		handle.dflt = 'handleDflt'
		handle.somekey = 'handleSomekey'
		Closure getRefered = {
			def refered = handle.refer(path)
			return (refered instanceof TeaHandle)? refered.path : refered
		}
		
		expect:
		getRefered.call(path) == expect
		
		where:
		path						|| expect
		'some:lower'				|| '/some:dec/some:handle/some:lower'
		'some#somekey'				|| 'lower2somekey'
		'some#'						|| 'lower2dflt'
		'#somekey'					|| 'handleSomekey'
		'#'							|| 'handleDflt'
		'..'						|| '/some:dec'
		'../some:handle'			|| '/some:dec/some:handle'
		'../some:handle#somekey'	|| 'handleSomekey'
		'../some:handle#'			|| 'handleDflt'
	}
	
	def 'findAll'(){
		given:
		TpacHandle handle = new TpacHandle(tag: 'some', name: 'handle')
		TpacHandle lower1 = new TpacHandle(tag: 'some')
		TpacHandle lower2 = new TpacHandle(tag: 'some', name: 'lower2')
		handle << lower1
		handle << lower2
		List list
		
		when: 'dflt以外の下位ハンドルを取得します'
		list = handle.findAll { it != 'some:dflt' }
		then:
		list.size() == 1
		list.collect { it.key } == [ 'some:lower2' ]
		
		when: 'タグが someの下位ハンドルを取得します'
		list = handle.findAll(/^some:.+$/)
		then:
		list.size() == 2
		list.collect { it.key } == [ 'some', 'some:lower2' ]
		
		when: 'みつからない場合は空リストを返します'
		list = handle.findAll(/nosuch/)
		then:
		list.size() == 0
	}
	
	def 'scan'(){
		given:
		TpacHandle handle = new TpacHandle(tag: 'some', name: 'handle')
		TpacHandle lower1 = new TpacHandle(tag: 'some')
		TpacHandle lower2 = new TpacHandle(tag: 'some', name: 'lower2')
		handle << lower1
		handle << lower2
		
		when:
		handle.scan { def hndl -> hndl.boo = 'foo' }
		then:
		handle.boo == 'foo'
		lower1.boo == 'foo'
		lower2.boo == 'foo'
	}
	
	def 'validateKeys'(){
		given:
		TpacHandle handle
		Map conds
		
		when: '必須チェックをしたい場合はキー「required」にtrueを指定してください'
		conds = [ 'boo': [ 'required': true ] ]
		handle = new TpacHandle(tag: 'some', name: 'handle')
		handle.boo = 'foo'
		handle.validateKeys(conds)
		then:
		handle.boo == 'foo'
		
		when: 'キーに値が指定されていなければデフォルト値を格納します'
		conds = [ 'boo': [ 'dflt': 'hello' ] ]
		handle = new TpacHandle(tag: 'some', name: 'handle')
		handle.validateKeys(conds)
		then:
		handle.boo == 'hello'
		
		when: 'クラスのチェックをしたい場合はキー「types」には設定可能な値のクラスをリストで指定してください'
		conds = [ 'boo': [ 'types': [ Integer, null ] ] ]
		handle = new TpacHandle(tag: 'some', name: 'handle')
		handle.boo = 3
		handle.validateKeys(conds)
		then:
		handle.boo == 3
		
		when: 'クラスのチェックをしたい場合はキー「types」には設定可能な値のクラスをリストで指定してください'
		conds = [ 'boo': [ 'types': [ Integer, null ] ] ]
		handle = new TpacHandle(tag: 'some', name: 'handle')
		handle.boo = null
		handle.validateKeys(conds)
		then:
		handle.boo == null
	}
	
	def 'validateKeys - exception'(){
		given:
		TpacHandle handle
		Map conds
		TpacSemanticException exc
		
		when: 'キーに値が指定されていなければ例外を投げます'
		conds = [ 'boo': [ 'required': true ] ]
		handle = new TpacHandle(tag: 'some', name: 'handle')
		handle.validateKeys(conds)
		then:
		exc = thrown(TpacSemanticException)
		exc.message == String.format(msgs.validate.unspecifiedValue, 'boo')
		
		when: '値がリストにないクラスであれば例外を投げます'
		conds = [ 'boo': [ 'types': [ Integer ] ] ]
		handle = new TpacHandle(tag: 'some', name: 'handle')
		handle.boo = 'hello'
		handle.validateKeys(conds)
		then:
		exc = thrown(TpacSemanticException)
		exc.message == String.format(msgs.validate.invalidType, 'boo', String.class.name)
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
		handle['dflt'] = [ 'default value1', 'default value2' ]
		handle['key1'] = 'val1'
		handle['key2'] = [ 'val2', 'val3' ]
		handle['key3'] = ''
		handle['key4'] = '==='
		result = getString(handle)
		expected = '''\
			#> handle:hello
			#:comment 1
			#:comment 2
			#:comment 3
			#-key1 val1
			#-key3 _
			#-key4 _===
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
		null			|| 'null'
		true		|| 'true'
		false		|| 'false'
		-1			|| '-1'
		2.3			|| '2.3'
		TpacRefer.newInstance(new TpacHandle(tag: 'handle'), '..')	|| '@..'
		Pattern.compile(/.+/)	|| ':.+'
		new TpacEval('3+2')	|| '=3+2'
		"${'abc'}d${System.lineSeparator()}"	|| '_abcd\\r\\n'
		'null'		|| '_null'
		'true'		|| '_true'
		'false'		|| '_false'
		'@..'		|| '_@..'
		':.+'		|| '_:.+'
		'=3+2'		|| '_=3+2'
		'_hello'		|| '__hello'
		'\n'			|| '_\\n'
		'\r'			|| '_\\r'
		'\f'			|| '_\\f'
		'\b'			|| '_\\b'
		'\t'			|| '_\\t'
		"'"			|| "_\\'"
		'"'			|| '_\\"'
		'あ\nん'		|| '_あ\\nん'
		'\fい\r'		|| '_\\fい\\r'
		"'Hello'"		|| "_\\'Hello\\'"
		'山"い\t川"'	|| '_山\\"い\\t川\\"'
		'\\n'		|| '_\\\\n'
		'\\\n'		|| '_\\\\\\n'
		''			|| '_'
		'abc'		|| 'abc'
		'あいう'		|| 'あいう'
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
		cloned.lowers['lower:dflt'].key == lower.key
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
