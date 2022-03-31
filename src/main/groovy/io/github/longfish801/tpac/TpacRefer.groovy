/*
 * TpacRefer.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.TpacMsg as msgs
import io.github.longfish801.tpac.tea.TeaHandle

/**
 * tpac文書の参照です。
 * @author io.github.longfish801
 */
class TpacRefer {
	/** ハンドル */
	TeaHandle handle
	/** パス */
	String path
	
	/**
	 * インスタンスを生成します。
	 * @param handle ハンドル
	 * @param fullpath アンカーを含みうるパス
	 */
	static TpacRefer newInstance(TeaHandle handle, String path){
		return new TpacRefer(handle: handle, path: path)
	}
	
	/**
	 * 文字列表現を返します。
	 * @return 文字列
	 */
	@Override
	String toString(){
		return path
	}
	
	/**
	 * パスに対応する要素を参照します。
	 * @return パスに対応する要素
	 */
	def refer(){
		return handle.refer(path)
	}
}
