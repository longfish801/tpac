/*
 * TpacReferSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.util.logging.Slf4j
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
	@Shared TpacDec handle
	
	def setup(){
		handle = new TpacDec()
		handle.tag = 'some'
		handle.name = 'thing'
		handle.happend = 'OK?'
		handle._ = 'OK!'
	}
	
	def 'newInstance'(){
		given:
		TpacRefer refer
		
		when:
		refer = TpacRefer.newInstance(handle, 'some:thing')
		then:
		refer.handle == handle
		refer.path == 'some:thing'
		refer.anchor == null
		
		when: 'アンカー有の場合'
		refer = TpacRefer.newInstance(handle, 'some:thing#happend')
		then:
		refer.handle == handle
		refer.path == 'some:thing'
		refer.anchor == 'happend'
		
		when: 'アンカー有でデフォルトキーの場合'
		refer = TpacRefer.newInstance(handle, 'some:thing#_')
		then:
		refer.handle == handle
		refer.path == 'some:thing'
		refer.anchor == '_'
	}
	
	def 'newInstance - exception'(){
		given:
		TpacHandlingException exc
		
		when:
		TpacRefer.newInstance(handle, 'some:thing#')
		then:
		exc = thrown(TpacHandlingException)
		exc.message == String.format(msgs.exc.noEmptyAnchor, 'some:thing#')
	}
	
	def 'constructor'(){
		given:
		TpacRefer refer
		
		when:
		refer = new TpacRefer(handle)
		then:
		refer instanceof TpacRefer
		refer.handle == handle
	}
	
	def 'toString'(){
		given:
		TpacRefer refer
		
		when:
		refer = TpacRefer.newInstance(handle, 'some:thing')
		then:
		refer.toString() == 'some:thing'
		
		when: 'アンカー有の場合'
		refer = TpacRefer.newInstance(handle, 'some:thing#happend')
		then:
		refer.toString() == 'some:thing#happend'
		
		when: 'アンカー有でデフォルトキーの場合'
		refer = TpacRefer.newInstance(handle, 'some:thing#_')
		then:
		refer.toString() == 'some:thing#_'
	}
	
	def 'refer'(){
		given:
		TpacRefer refer
		
		when:
		refer = TpacRefer.newInstance(handle, 'some:thing')
		then:
		refer.refer() == handle
		
		when: 'アンカー有の場合'
		refer = TpacRefer.newInstance(handle, 'some:thing#happend')
		then:
		refer.refer() == 'OK?'
		
		when: 'アンカー有でデフォルトキーの場合'
		refer = TpacRefer.newInstance(handle, 'some:thing#_')
		then:
		refer.refer() == 'OK!'
	}
}
