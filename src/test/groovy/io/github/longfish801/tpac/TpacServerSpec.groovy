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
		
		when: '同じ識別キーがあるときはマージされること'
		source = '''\
			#! tpac:some
			#-key1 one
			#-key2 two
			#! tpac:some
			#-key2 TWO
			#-key3 three
			'''.stripIndent()
		server.soak(source)
		then:
		server['tpac:some'].key == 'tpac:some'
		server['tpac:some'].map['key1'] == 'one'
		server['tpac:some'].map['key2'] == 'TWO'
		server['tpac:some'].map['key3'] == 'three'
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
	def 'solve'(){
		given:
		TpacDec dec = new TpacDec(tag: 'dec')
		TpacHandle handle = new TpacHandle(tag: 'handle')
		server << dec
		dec << handle
		
		expect:
		server.solve(path).path == expect
		
		where:
		path			|| expect
		'/dec'			|| '/dec'
		'/dec/handle'	|| '/dec/handle'
	}
	
	def 'solve - exception'(){
		given:
		TpacHandlingException exc
		
		when:
		server.solve('x')
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
		
		when: 'デフォルトキー以外の宣言を取得します'
		list = server.findAll { it != 'some:dflt' }
		then:
		list.size() == 1
		list.collect { it.key } == [ 'some:dec2' ]
		
		when: 'タグが someの宣言を取得します'
		list = server.findAll(/^some:.+$/)
		then:
		list.size() == 2
		list.collect { it.key } == [ 'some', 'some:dec2' ]
		
		when: 'みつからない場合は空リストを返します'
		list = server.findAll(/nosuch/)
		then:
		list.size() == 0
	}
}
