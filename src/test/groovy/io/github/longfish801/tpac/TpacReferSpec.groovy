/*
 * TpacReferSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.util.logging.Slf4j
import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.TpacMsg as msgs
import spock.lang.Specification
import spock.lang.Shared

/**
 * TpacReferクラスのテスト。
 * @version 0.3.00 2020/05/27
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacReferSpec extends Specification {
	@Shared TpacDec dec
	@Shared TpacHandle handle
	
	def setup(){
		dec = new TpacDec(tag: 'dec')
		handle = new TpacHandle(tag: 'some', name: 'thing')
		handle.happend = 'OK?'
		handle._ = 'OK!'
		dec << handle
	}
	
	def 'newInstance'(){
		given:
		TpacRefer refer
		
		when:
		refer = TpacRefer.newInstance(dec, 'some:thing')
		then:
		refer.handle == dec
		refer.path == 'some:thing'
		refer.anchor == null
		
		when: 'アンカー有の場合'
		refer = TpacRefer.newInstance(dec, 'some:thing#happend')
		then:
		refer.handle == dec
		refer.path == 'some:thing'
		refer.anchor == 'happend'
		
		when: 'アンカー有でデフォルトキーの場合'
		refer = TpacRefer.newInstance(dec, 'some:thing#')
		then:
		refer.handle == dec
		refer.path == 'some:thing'
		refer.anchor == cnst.dflt.mapKey
	}
	
	def 'constructor'(){
		given:
		TpacRefer refer
		
		when:
		refer = new TpacRefer(dec)
		then:
		refer instanceof TpacRefer
		refer.handle == dec
	}
	
	def 'toString'(){
		given:
		TpacRefer refer
		
		when:
		refer = TpacRefer.newInstance(dec, 'some:thing')
		then:
		refer.toString() == 'some:thing'
		
		when: 'アンカー有の場合'
		refer = TpacRefer.newInstance(dec, 'some:thing#happend')
		then:
		refer.toString() == 'some:thing#happend'
		
		when: 'アンカー有でデフォルトキーの場合'
		refer = TpacRefer.newInstance(dec, 'some:thing#')
		then:
		refer.toString() == 'some:thing#'
	}
	
	def 'refer'(){
		given:
		TpacRefer refer
		
		when:
		refer = TpacRefer.newInstance(dec, 'some:thing')
		then:
		refer.refer() == handle
		
		when: 'アンカー有の場合'
		refer = TpacRefer.newInstance(dec, 'some:thing#happend')
		then:
		refer.refer() == 'OK?'
		
		when: 'アンカー有でデフォルトキーの場合'
		refer = TpacRefer.newInstance(dec, 'some:thing#')
		then:
		refer.refer() == 'OK!'
	}
}
