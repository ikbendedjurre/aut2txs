/*
 * generated by Xtext 2.18.0.M3
 */
package org.aut2txs.tool


/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class AldebaranLangStandaloneSetup extends AldebaranLangStandaloneSetupGenerated {

	def static void doSetup() {
		new AldebaranLangStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
}
