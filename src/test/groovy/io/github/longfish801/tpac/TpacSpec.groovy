/*
 * TpacSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.lang.ExistResource;
import io.github.longfish801.shared.util.PackageDirectory;
import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * Tpacクラスのテスト。
 * @version 1.0.00 2017/07/04
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacSpec extends Specification {
	/** ファイル入出力のテスト用フォルダ */
	private static final File testDir = PackageDirectory.deepDir('src/test/resources', TpacSpec.class);
	
	def 'ファイル内容をTPAC文書とみなして解析結果を保持するコンストラクタです'(){
		when:
		new Tpac(new File(testDir, 'toStr.tpac'));
		then:
		noExceptionThrown();
	}
	
	def 'URL先の内容をTPAC文書とみなして解析結果を保持するコンストラクタです'(){
		when:
		new Tpac(new ExistResource(TpacSpec.class).get('TpacSpec/toStr.tpac'));
		then:
		noExceptionThrown();
	}
	
	def 'TPAC記法の文字列を解析し結果を保持するコンストラクタです'(){
		when:
		new Tpac("#! dec ルート\n");
		then:
		noExceptionThrown();
	}
	
	def '文字列表現を返します'(){
		given:
		Tpac tpac = null;
		
		when:
		tpac = new Tpac(new File(testDir, 'toStr.tpac'));
		then:
		tpac.toString() == new File(testDir, 'toStr.tpac').getText();
		
		when:
		tpac = new Tpac(new ExistResource(TpacSpec.class).get('TpacSpec/toStr.tpac'));
		then:
		tpac.toString() == new File(testDir, 'toStr.tpac').getText();
	}
	
	@Unroll
	def 'XML形式で表現した文字列を返します'(){
		expect:
		new Tpac(new File(testDir, "${target}.tpac")).toXml() == new File(testDir, "${expect}.xml").getText();
		
		where:
		target	|| expect
		'01'	|| '01'	// 宣言のみ
		'02'	|| '02'	// 親要素
		'03'	|| '03'	// 子要素
		'04'	|| '04'	// 属性
		'05'	|| '05'	// テキスト
		'06'	|| '06'	// 混在
	}
	
	def 'このTPAC文書を指定されたTPAC文書で上書きします'(){
		given:
		Tpac tpacSrc = new Tpac(new File(testDir, 'blendSrc.tpac'));
		Tpac tpacMix = new Tpac(new File(testDir, 'blendMix.tpac'));
		Tpac tpacRes = new Tpac(new File(testDir, 'blendRes.tpac'));
		
		when:
		tpacSrc.blend(tpacMix);
		
		then:
		tpacSrc.toString() == tpacRes.toString();
	}
}
