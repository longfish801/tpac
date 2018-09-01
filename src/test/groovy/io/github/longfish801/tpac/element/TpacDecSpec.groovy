/*
 * TpacDecSpec.groovy
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
 * TpacDecクラスのテスト。
 * @version 1.0.00 2018/08/26
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacDecSpec extends Specification {
	@Shared TeaServer server;
	
	def setup(){
		server = new TeaServer();
	}
	
	def 'タグ、名前を格納します。'(){
		given:
		TeaDec dec = new TpacDec();
		
		when:
		dec.setup('tpac', 'dec', server);
		then:
		dec.tag == 'tpac';
		dec.name == 'dec';
		dec.server == server;
		
		when:
		dec.setup('tpac', '', server);
		then:
		dec.name == '';
		dec.server == server;
	}
	
	def 'この宣言の階層を返します。'(){
		given:
		TeaDec dec = new TpacDec().setup('tpac', 'dec', server);
		
		expect:
		dec.level == 0;
	}
	
	def 'この宣言のパスを返します。'(){
		given:
		TeaDec dec = new TpacDec().setup('tpac', 'dec', server);
		
		expect:
		dec.path == '/tpac:dec';
	}
	
	@Unroll
	def 'パスに対応するハンドルを返します。'(){
		given:
		TeaDec dec = new TpacDec().setup('tpac', 'dec', server);
		TeaHandle handle = new TpacHandle().setup('some', 'thing', dec);
		
		expect:
		dec.path(path).path == expect;
		
		where:
		path					|| expect
		'some:thing'			|| '/tpac:dec/some:thing';
		'/tpac:dec/some:thing'	|| '/tpac:dec/some:thing';
	}
}
