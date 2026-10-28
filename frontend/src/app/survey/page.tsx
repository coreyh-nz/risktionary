import { redirect } from "next/navigation"

// server-only: the actual survey destination. NEXT_PUBLIC_SURVEY_URL (see
// src/lib/config.ts) is the separate, publicly shown link/label.
const SurveyPage = () => {
  redirect(process.env.SURVEY_REDIRECT_URL ?? "")
}

export default SurveyPage
