/*
 * TpacEvalSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.util.logging.Slf4j
import spock.lang.Specification

/**
 * TpacEvalクラスのテスト。
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacEvalSpec extends Specification {
	def 'constructor'(){
		given:
		TpacEval eval
		
		when:
		eval = TpacEval.newInstance('3 + 2')
		then:
		eval instanceof TpacEval
		eval.expression == '3 + 2'
	}
	
	def 'eval'(){
		given:
		TpacEval eval
		
		when:
		eval = TpacEval.newInstance('3 + 2')
		then:
		eval.eval() == 5
	}
}
