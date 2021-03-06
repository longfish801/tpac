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
 * @version 0.3.00 2020/05/29
 * @author io.github.longfish801
 */
class TpacRefer {
	/** ハンドル */
	TeaHandle handle
	/** パス */
	String path
	/** アンカー */
	String anchor = null
	
	/**
	 * インスタンスを生成します。
	 * @param handle ハンドル
	 * @param fullpath アンカーを含みうるパス
	 */
	static TpacRefer newInstance(TeaHandle handle, String fullpath){
		TpacRefer refer = new TpacRefer(handle)
		int anchorIdx = fullpath.indexOf(cnst.scalar.anchor)
		if (anchorIdx < 0){
			refer.path = fullpath
		} else {
			refer.path = fullpath.substring(0, anchorIdx)
			refer.anchor = fullpath.substring(anchorIdx + cnst.scalar.anchor.length())
			if (refer.anchor == cnst.omit.mapKey) refer.anchor = cnst.dflt.mapKey
		}
		return refer
	}
	
	/**
	 * コンストラクタ。
	 * @param handle ハンドル
	 */
	TpacRefer(TeaHandle handle) {
		this.handle = handle
	}
	
	/**
	 * 文字列表現を返します。
	 * @return 文字列
	 */
	@Override
	String toString(){
		return (anchor == null)? path : "${path}${cnst.path.anchor}${(anchor == cnst.dflt.mapKey)? '' : anchor}"
	}
	
	/**
	 * パスに対応する要素を参照します。
	 * @return パスに対応する要素
	 */
	def refer(){
		return (anchor == null)? handle.solvePath(path) : handle.solvePath(path)?.getAt(anchor)
	}
}
