/*
 * TpacServerSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.util.logging.Slf4j
import io.github.longfish801.tpac.TpacMsg as msgs
import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared

/**
 * TpacServerクラスのテスト。
 * @version 0.3.00 2020/06/03
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacServerSpec extends Specification {
	/** TpacServer */
	@Shared TpacServer server
	
	def setup(){
		server = new TpacServer()
	}
	
	def 'newMaker'(){
		given:
		TpacMaker maker
		
		when:
		maker = server.newMaker('tpac')
		then:
		maker instanceof TpacMaker
	}
	
	def 'newParty'(){
		given:
		TpacParty party
		
		when:
		party = server.newParty()
		then:
		party instanceof TpacParty
		party.server == server
	}
	
	def 'soak'(){
		given:
		String source
		
		when:
		source = '''\
			#! tpac
			#! tpac:second
			'''.stripIndent()
		server.soak(source)
		then:
		server['tpac'].key == 'tpac'
		server['tpac:second'].key == 'tpac:second'
	}
	
	def 'leftShift'(){
		given:
		TpacDec dec = new TpacDec(tag: 'dec')
		
		when:
		server << dec
		then:
		dec.server == server
		server.decs['dec'] == dec
	}
	
	def 'getAt'(){
		given:
		TpacDec dec = new TpacDec(tag: 'dec', name: 'some')
		
		when:
		server << dec
		then:
		server['dec:some'] == dec
	}
	
	def 'propertyMissing'(){
		given:
		TpacDec dec = new TpacDec(tag: 'dec')
		
		when:
		server << dec
		then:
		server.dec == dec
	}
	
	@Unroll
	def 'solvePath'(){
		given:
		TpacDec dec = new TpacDec(tag: 'dec')
		TpacHandle handle = new TpacHandle(tag: 'handle')
		server << dec
		dec << handle
		
		expect:
		server.solvePath(path).path == expect
		
		where:
		path			|| expect
		'/dec'			|| '/dec'
		'/dec/handle'	|| '/dec/handle'
	}
	
	def 'solvePath - exception'(){
		given:
		TpacHandlingException exc
		
		when:
		server.solvePath('x')
		then:
		exc = thrown(TpacHandlingException)
		exc.message == String.format(msgs.exc.invalidpath, 'x')
	}
	
	def 'findAll'(){
		given:
		TpacDec dec1 = new TpacDec(tag: 'some')
		TpacDec dec2 = new TpacDec(tag: 'some', name: 'dec2')
		server << dec1
		server << dec2
		List list
		
		when: 'タグが someで、名前が空文字ではない宣言を取得します'
		list = server.findAll(/^some:/)
		then:
		list.size() == 1
		list.collect { it.key } == [ 'some:dec2' ]
		
		when: 'タグが someの宣言を取得します'
		list = server.findAll(/^some|some:.+$/)
		then:
		list.size() == 2
		list.collect { it.key } == [ 'some', 'some:dec2' ]
		
		when: 'みつからない場合は空リストを返します'
		list = server.findAll(/nosuch/)
		then:
		list.size() == 0
	}
}
