/*
 * TpacParseExceptionsSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.util.logging.Slf4j
import spock.lang.Specification

/**
 * TpacParseExceptionsクラスのテスト。
 * @version 0.3.00 2020/05/01
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacParseExceptionsSpec extends Specification {
	def 'getLocalizedMessage'(){
		given:
		TpacParseExceptions exc
		
		when:
		exc = new TpacParseExceptions('Parse Error!')
		then:
		exc.errors.empty
		exc.toString() == 'io.github.longfish801.tpac.TpacParseExceptions: Parse Error!'
		
		when:
		exc = new TpacParseExceptions('Parse Error!')
		exc.append(3, 'Wow!', new Exception('Great!'))
		exc.append(7, 'How?', new Exception('Fine!'))
		then:
		exc.errors.size() == 2
		exc.toString() == '''\
			io.github.longfish801.tpac.TpacParseExceptions: Parse Error!
				lineNo=3 line=Wow!
					java.lang.Exception: Great!
				lineNo=7 line=How?
					java.lang.Exception: Fine!'''.stripIndent().denormalize()
	}
}
