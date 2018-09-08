/*
 * TpacRefer.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.ArgmentChecker;
import io.github.longfish801.tpac.TeaServer;

/**
 * tpac文書の参照です。
 * @version 1.0.00 2018/08/10
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacRefer {
	/** ハンドル */
	TeaHandle handle;
	/** パス */
	String path;
	/** アンカー */
	String anchor = '';
	
	/**
	 * 新規インスタンスを生成します。
	 * @param path パス
	 * @param handle ハンドル
	 */
	static TpacRefer newInstance(String path, TeaHandle handle){
		ArgmentChecker.checkNotNull('パス', path);
		TpacRefer refer = new TpacRefer(handle);
		int anchorIdx = path.indexOf(TpacHandle.cnstTeaHandle.path.anchor);
		refer.path = (anchorIdx < 0)? path : path.substring(0, anchorIdx);
		if (anchorIdx >= 0) refer.anchor = path.substring(anchorIdx + TpacHandle.cnstTeaHandle.path.anchor.length());
		return refer;
	}
	
	/**
	 * コンストラクタ。
	 * @param handle ハンドル
	 */
	TpacRefer(TeaHandle handle) {
		ArgmentChecker.checkNotNull('ハンドル', handle);
		this.handle = handle;
	}
	
	/**
	 * 文字列表現を返します。
	 * @return 文字列
	 */
	@Override
	String toString(){
		return (anchor.empty)? path : "${path}${TpacHandle.cnstTeaHandle.path.anchor}${anchor}";
	}
	
	/**
	 * パスに対応する要素を参照します。
	 * @return パスに対応する要素
	 */
	def refer(){
		return (anchor.empty)? handle.path(path) : Eval.x(handle.path(path), "x.${anchor}");
	}
}
