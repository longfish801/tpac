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
		handle.dflt = 'OK!'
		dec << handle
	}
	
	def 'newInstance'(){
		given:
		TpacRefer refer
		
		when:
		refer = TpacRefer.newInstance(dec, 'some:thing#happend')
		then:
		refer.handle == dec
		refer.path == 'some:thing#happend'
	}
	
	def 'toString'(){
		given:
		TpacRefer refer
		
		when:
		refer = TpacRefer.newInstance(dec, 'some:thing')
		then:
		refer.toString() == 'some:thing'
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
