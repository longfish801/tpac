/*
 * TpacReferSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.tpac.TeaServer;
import spock.lang.Specification;
import spock.lang.Shared;

/**
 * TpacReferクラスのテスト。
 * @version 1.0.00 2018/08/26
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacReferSpec extends Specification {
	@Shared TeaHandle handle;
	
	def setup(){
		handle = new TpacDec().setup('tag', 'name', new TeaServer());
	}
	
	def '新規インスタンスを生成します。'(){
		given:
		TpacRefer refer;
		
		when:
		refer = TpacRefer.newInstance('some:thing', handle);
		then:
		refer instanceof TpacRefer;
		refer.handle == handle;
		refer.path == 'some:thing';
		refer.anchor == '';
		
		when:
		refer = TpacRefer.newInstance('some:thing#happend', handle);
		then:
		refer instanceof TpacRefer;
		refer.handle == handle;
		refer.path == 'some:thing';
		refer.anchor == 'happend';
		
		when:
		refer = TpacRefer.newInstance('', handle);
		then:
		refer.path == '';
		refer.anchor == '';
		
		when:
		refer = TpacRefer.newInstance('#happend', handle);
		then:
		refer.path == '';
		refer.anchor == 'happend';
		
		when:
		refer = TpacRefer.newInstance(null, handle);
		then:
		thrown(IllegalArgumentException);
		
		when:
		refer = TpacRefer.newInstance('some:thing', null);
		then:
		thrown(IllegalArgumentException);
	}
	
	def '文字列表現を返します。'(){
		given:
		TpacRefer refer;
		
		when:
		refer = TpacRefer.newInstance('some:thing', handle);
		then:
		refer.toString() == 'some:thing';
		
		when:
		refer = TpacRefer.newInstance('some:thing#happend', handle);
		then:
		refer.toString() == 'some:thing#happend';
	}
	
	def 'パスに対応する要素を参照します。'(){
		given:
		TpacRefer refer;
		handle.scalar = 'Hello';
		
		when:
		refer = TpacRefer.newInstance('', handle);
		then:
		refer.refer() == handle;
		
		when:
		refer = TpacRefer.newInstance('#scalar', handle);
		then:
		refer.refer() == 'Hello';
	}
}
