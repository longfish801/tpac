/*
 * TpacEval.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

/**
 * tpac文書のGroovy評価値です。
 * @author io.github.longfish801
 */
class TpacEval {
	/** 評価対象の値 */
	String expression
	
	/**
	 * インスタンスを生成します。
	 * @param expression 評価対象の値
	 * @return インスタンス
	 */
	static TpacEval newInstance(String expression){
		return new TpacEval(expression: expression)
	}
	
	/**
	 * 評価対象の値を評価した結果を返します。<br/>
	 * {@link groovy.util.Eval#me(String)}の引数に
	 * 評価対象の値を渡した戻り値を返します。
	 * @return 評価結果
	 */
	def eval(){
		return Eval.me(expression)
	}
}
