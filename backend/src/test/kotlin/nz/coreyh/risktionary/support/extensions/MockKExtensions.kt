package nz.coreyh.risktionary.support.extensions

import io.mockk.mockk

inline fun <reified T : Any> mockkRelaxed(block: T.() -> Unit = {}): T = mockk(relaxed = true, block = block)
