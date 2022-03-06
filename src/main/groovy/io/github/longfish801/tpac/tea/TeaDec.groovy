/*
 * TeaDec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.tea

import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.tea.TeaServer

/**
 * 宣言の特性です。
 * @version 0.3.00 2020/05/23
 * @author io.github.longfish801
 */
trait TeaDec extends TeaHandle {
	/** TeaServer */
	TeaServer server
	
	/**
	 * この宣言の階層(0)を返します。
	 * @return 階層
	 */
	@Override
	int getLevel(){
		return 0
	}
	
	/**
	 * 宣言（自インスタンス）を返します。
	 * @return 宣言
	 */
	@Override
	TeaDec getDec(){
		return this
	}
	
	/**
	 * この宣言の絶対パスを返します。
	 * @return 絶対パス
	 */
	@Override
	String getPath(){
		return "${cnst.path.level}${key}" as String
	}
	
	/**
	 * パスに対応するハンドルを返します。<br/>
	 * 絶対パスの場合はサーバに解決を依頼します。<br/>
	 * 相対パスの場合は親クラスのメソッドを呼びます。
	 * @param path パス
	 * @return パスに対応するハンドル（該当するハンドルがなければnull）
	 */
	@Override
	TeaHandle solve(String path){
		return (path.startsWith(cnst.path.level))? server.solve(path) : super.solve(path)
	}
	
	/**
	 * 文字列表現を出力します。<br/>
	 * まず親クラスのメソッドを呼びます。<br/>
	 * そして宣言終端を出力します。
	 * @param writer Writer
	 */
	@Override
	void write(Writer writer){
		super.write(writer)
		// 宣言終端
		writer << cnst.bullet.decEnd
		writer << System.lineSeparator()
		writer << System.lineSeparator()
	}
}
