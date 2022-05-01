/*
 * TpacDecSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared

/**
 * TpacDecクラスのテスト。
 * @author io.github.longfish801
 */
class TpacDecSpec extends Specification {
	@Shared TpacServer server
	
	def setup(){
		server = new TpacServer()
	}
	
	def 'getLevel'(){
		expect:
		new TpacDec(tag: 'dec', server: server).level == 0
	}
	
	def 'getDec'(){
		given:
		TpacDec dec
		
		when:
		dec = new TpacDec(tag: 'dec', server: server)
		then:
		dec.dec == dec
	}
	
	def 'getPath'(){
		given:
		TpacDec dec
		
		when:
		dec = new TpacDec(tag: 'dec', server: server)
		then:
		dec.path == '/dec'
	}
	
	@Unroll
	def 'solve'(){
		given:
		TpacDec dec = new TpacDec(tag: 'dec', server: server)
		TpacHandle handle = new TpacHandle(tag: 'handle')
		server << dec
		dec << handle
		
		expect:
		dec.solve(path).path == expect
		
		where:
		path			|| expect
		'handle'		|| '/dec/handle'
		'/dec/handle'	|| '/dec/handle'
	}
	
	def 'write'(){
		given:
		Closure getString = { TpacDec dec ->
			StringWriter writer = new StringWriter()
			dec.write(writer)
			return writer.toString()
		}
		TpacDec dec
		TpacHandle handle
		String result
		String expected
		
		when:
		dec = new TpacDec(tag: 'dec', server: server)
		handle = new TpacHandle(tag: 'handle')
		dec << handle
		result = getString(dec)
		expected = '''\
			#! dec
			#>
			
			#> handle
			#>
			
			#!
			
			'''.stripIndent().denormalize()
		then:
		result == expected
	}
}
