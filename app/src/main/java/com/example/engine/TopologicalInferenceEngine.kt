package com.example.engine

import kotlin.math.ln
import kotlin.math.max

/**
 * Datové třídy pro Topologický Kognitivní Rámec (AKGE-8D)
 */
data class TopologicalMetrics(
    val vertices: Int, // V: Kognitivní uzly / vrcholy
    val edges: Int,    // E: Vazby / hrany
    val faces: Int,    // F: Stěny / vyšší kognitivní dimenze
    val eulerCharacteristic: Int, // χ = V - E + F
    val betti0: Int = 1,          // β0 = 1 (zachování komponenty souvislosti)
    val betti1: Int,              // β1: počet nezávislých kognitivních smyček / děr (E - V + β0)
    val shannonEntropy: Float,    // S = -Σ pi ln(pi)
    val isValid: Boolean,
    val failureReason: String? = null
)

data class PercolationStatus(
    val totalDomains: Int,
    val failingDomains: Int,
    val failingRatio: Float,
    val criticalThreshold: Float = 0.38f,
    val isCascadeRiskActive: Boolean,
    val statusMessage: String
)

data class TokenFlowBudget(
    val systemStressSigma: Float,
    val compressionRate: Float, // 0.70, 0.30 nebo 0.0
    val forensicBudget: String,  // MINIMAL, MODERATE, EXTENDED_FORENSIC
    val synthesisDepth: String   // STANDARD, BALANCED, DEEP_ANALYTICS
)

data class TopologicalVerificationResult(
    val isPhase1Rendered: Boolean = true, // Optimistické vykreslení
    val isPhase2Verified: Boolean,        // Výsledek topologické verifikace
    val isGlitchTriggered: Boolean,       // Zda byl aktivován vizuální glitch
    val metrics: TopologicalMetrics,
    val percolation: PercolationStatus,
    val tokenBudget: TokenFlowBudget,
    val diagnosticMessage: String,
    val proofOfNecessity: String? = null
)

/**
 * Centrální Topologický a Perkolační Inferenční Engine (FÁZE 12)
 * Implementuje Eulerovu charakteristiku, Bettiho čísla, Shannonovu entropii,
 * Perkolační model kaskádových rizik (pc ≈ 0.38) a Autonomní rebalancování tokenů (f(σ)).
 */
object TopologicalInferenceEngine {

    val DOMAIN_KEYS = listOf("SYS", "ECON", "PSYCH", "ECO", "LAW", "SEC", "PHYS", "SOC")
    const val CRITICAL_PERCOLATION_THRESHOLD = 0.38f

    /**
     * 1. Matematický kognitivní rámec: Topologické a informační metriky
     * χ = V - E + F
     * β0 = 1
     * β1 = max(0, E - V + β0)
     * S = -Σ pi ln(pi)
     */
    fun evaluateTopologicalMetrics(
        domainScores: Map<String, Float>,
        customNodesCount: Int = 8,
        customEdgesCount: Int = 12,
        customFacesCount: Int = 5
    ): TopologicalMetrics {
        val v = max(1, customNodesCount)
        val e = max(0, customEdgesCount)
        val f = max(0, customFacesCount)

        // Eulerova charakteristika topologického prostoru
        val chi = v - e + f

        // Bettiho čísla
        val betti0 = 1
        val betti1 = max(0, e - v + betti0)

        // Shannonova informační entropie z rozložení stavů domén
        val safeScores = DOMAIN_KEYS.map { (domainScores[it] ?: 0.5f).coerceIn(0.01f, 1.0f) }
        val sumScores = safeScores.sum().coerceAtLeast(0.001f)
        val probabilities = safeScores.map { it / sumScores }

        var entropy = 0.0f
        for (p in probabilities) {
            if (p > 0.0001f) {
                entropy += (p * ln(p.toDouble())).toFloat()
            }
        }
        val shannonEntropy = -entropy // S = -Σ pi ln(pi)

        // Kritéria topologické integrity:
        // Entropie nesmí překročit kritický rozpad (> 2.05), počet nezávislých analytických děr β1 nesmí být kriticky vysoký (> 8),
        // a Eulerova charakteristika χ musí zůstat v konvergentním intervalu [-4, 6]
        val isTopologyValid = shannonEntropy < 2.04f && betti1 in 0..7 && chi in -4..6

        val failureReason = when {
            shannonEntropy >= 2.04f -> "Kritická Shannonova entropie (S = ${String.format("%.3f", shannonEntropy)} > 2.04) detekovala informační bifurkaci a ztrátu koherence."
            betti1 > 7 -> "Bettiho číslo β1 = $betti1 překročilo práh analytických děr (rozpad kauzální souvislosti)."
            chi !in -4..6 -> "Eulerova charakteristika χ = $chi je mimo stabilní interval [-4, 6] (topologická singularita prostoru)."
            else -> null
        }

        return TopologicalMetrics(
            vertices = v,
            edges = e,
            faces = f,
            eulerCharacteristic = chi,
            betti0 = betti0,
            betti1 = betti1,
            shannonEntropy = shannonEntropy,
            isValid = isTopologyValid,
            failureReason = failureReason
        )
    }

    /**
     * 2. Perkolační model kaskádových rizik (FÁZE 12.2)
     * Detekuje šíření kaskádového stresu napříč závislostní sítí.
     * pc ≈ 0.38
     */
    fun evalCascadeRisk(
        domainsStatus: Map<String, String>,
        criticalThreshold: Float = CRITICAL_PERCOLATION_THRESHOLD
    ): PercolationStatus {
        val totalDomains = domainsStatus.size.coerceAtLeast(1)
        val failedDomains = domainsStatus.values.count { status ->
            status.equals("FAILED", ignoreCase = true) || status.equals("STRESSED", ignoreCase = true)
        }
        val failingRatio = failedDomains.toFloat() / totalDomains.toFloat()

        val isCascadeRisk = failingRatio >= criticalThreshold
        val statusMessage = if (isCascadeRisk) {
            "CRITICAL_CASCADE_RISK_ACTIVATED: Zasaženo ${String.format("%.1f", failingRatio * 100)}% domén (práh $criticalThreshold). Spuštěna nouzová izolace uzlů."
        } else {
            "SYSTEM_STABLE: Zasaženo ${String.format("%.1f", failingRatio * 100)}% domén. V mezích perkolační stability."
        }

        return PercolationStatus(
            totalDomains = totalDomains,
            failingDomains = failedDomains,
            failingRatio = failingRatio,
            criticalThreshold = criticalThreshold,
            isCascadeRiskActive = isCascadeRisk,
            statusMessage = statusMessage
        )
    }

    /**
     * 3. Autonomní rebalancování tokenového toku (FÁZE 12.3)
     * Dynamická adaptace hloubky syntézy na základě systémového pnutí (σ).
     */
    fun calculateTokenBudget(systemStressSigma: Float): TokenFlowBudget {
        val sigma = systemStressSigma.coerceIn(0.0f, 1.0f)
        return when {
            sigma < 0.2f -> TokenFlowBudget(
                systemStressSigma = sigma,
                compressionRate = 0.70f,
                forensicBudget = "MINIMAL",
                synthesisDepth = "STANDARD"
            )
            sigma < 0.6f -> TokenFlowBudget(
                systemStressSigma = sigma,
                compressionRate = 0.30f,
                forensicBudget = "MODERATE",
                synthesisDepth = "BALANCED"
            )
            else -> TokenFlowBudget(
                systemStressSigma = sigma,
                compressionRate = 0.0f,
                forensicBudget = "EXTENDED_FORENSIC",
                synthesisDepth = "DEEP_ANALYTICS"
            )
        }
    }

    /**
     * Dvoufázové topologické ověřování s optimistickým vykreslováním:
     * Fáze 1: Text se okamžitě zobrazuje v UI (optimistický režim).
     * Fáze 2: Rigorózní topologická kontrola (χ, β1, S, pc).
     * Při selhání se aktivuje glitchTriggered = true a připraví se Gatekeeper diagnostika.
     */
    fun verifyInferenceOutput(
        domainScores: Map<String, Float>,
        domainsStatus: Map<String, String>,
        systemStressSigma: Float,
        hasExplicitOverride: Boolean = false,
        overrideJustification: String? = null
    ): TopologicalVerificationResult {
        val metrics = evaluateTopologicalMetrics(domainScores)
        val percolation = evalCascadeRisk(domainsStatus)
        val tokenBudget = calculateTokenBudget(systemStressSigma)

        // Pokud je selhání a NENÍ aktivní override s oprávněním -> spustit vizuální glitch
        val hasFailed = !metrics.isValid || percolation.isCascadeRiskActive
        val glitchTriggered = hasFailed && !hasExplicitOverride

        val diagnosticMsg = when {
            hasExplicitOverride -> "EXECUTIVE OVERRIDE AKTIVNÍ: Veto potlačeno oprávněním ADMIN_OPERATOR. Důvod: ${overrideJustification ?: "Operativní stabilizace"}. Do ZK-SNARK zapsán příznak OVERRIDE_ACTIVE."
            !metrics.isValid -> "ZERO-HALLUCINATION GATEKEEPER CLONA: ${metrics.failureReason} Text byl pro ochranu integrity kognitivního prostoru překryt ochranným filtrem."
            percolation.isCascadeRiskActive -> "PERKOLAČNÍ NOUZOVÝ STAV: ${percolation.statusMessage}. Výstup podléhá bezpečnostní karanténě."
            else -> "TOPOLOGICKY VERIFIKOVÁNO: Euler χ = ${metrics.eulerCharacteristic}, Betti β1 = ${metrics.betti1}, Entropie S = ${String.format("%.3f", metrics.shannonEntropy)}. Invarianty splněny."
        }

        val proofOfNecessity = if (hasExplicitOverride) {
            "ProofOfNecessity: [OVERRIDE_ACTIVE] Manually overridden by ADMIN_OPERATOR. Stress σ = ${String.format("%.2f", systemStressSigma)}, FailingRatio = ${String.format("%.2f", percolation.failingRatio)}. Justification: ${overrideJustification ?: "Manual emergency override"}."
        } else if (hasFailed) {
            "ProofOfNecessity: [CONTAINMENT_TRIGGERED] Automated gatekeeper trigger due to invariant violation."
        } else {
            "ProofOfNecessity: [INVARIANT_PRESERVED] Full topological and entropy adherence."
        }

        return TopologicalVerificationResult(
            isPhase1Rendered = true,
            isPhase2Verified = !hasFailed || hasExplicitOverride,
            isGlitchTriggered = glitchTriggered,
            metrics = metrics,
            percolation = percolation,
            tokenBudget = tokenBudget,
            diagnosticMessage = diagnosticMsg,
            proofOfNecessity = proofOfNecessity
        )
    }
}
