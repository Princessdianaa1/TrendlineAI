import os
import requests
from typing import Optional

class FinancialChatbot:
    def __init__(self):
        # Check for free LLM options
        self.use_huggingface = os.getenv('HUGGINGFACE_API_KEY') is not None
        self.use_ollama = self._check_ollama()
        
        print(f"Chatbot initialized:")
        print(f"  - Hugging Face: {'✓' if self.use_huggingface else '✗'}")
        print(f"  - Ollama: {'✓' if self.use_ollama else '✗'}")
        print(f"  - Fallback Mode: {'✓' if not self.use_huggingface and not self.use_ollama else '✗'}")
    
    def _check_ollama(self) -> bool:
        """Check if Ollama is running locally"""
        try:
            response = requests.get('http://localhost:11434/api/tags', timeout=2)
            if response.status_code == 200:
                data = response.json()
                models = data.get('models', [])
                if models:
                    print(f"  - Available models: {[m['name'] for m in models]}")
                    return True
                else:
                    print("  - Ollama running but no models found")
                    return False
            return False
        except:
            return False
    
    def get_response(self, question: str) -> str:
        """Get response for financial question"""
        
        if self.use_huggingface:
            try:
                return self._get_huggingface_response(question)
            except Exception as e:
                print(f"Hugging Face failed: {e}")
                return self._get_fallback_response(question)
        elif self.use_ollama:
            try:
                return self._get_ollama_response(question)
            except Exception as e:
                print(f"Ollama failed: {e}")
                return self._get_fallback_response(question)
        else:
            return self._get_fallback_response(question)
    
    def get_document_response(self, question: str, document_text: str) -> str:
        """Get response based on uploaded document"""
        
        context = f"Based on this financial document:\n\n{document_text[:2000]}\n\nQuestion: {question}"
        
        if self.use_huggingface:
            try:
                return self._get_huggingface_response(context)
            except:
                return f"Document contains {len(document_text)} characters. {self._get_fallback_response(question)}"
        elif self.use_ollama:
            try:
                return self._get_ollama_response(context)
            except:
                return f"Document contains {len(document_text)} characters. {self._get_fallback_response(question)}"
        else:
            return f"Document contains {len(document_text)} characters. {self._get_fallback_response(question)}"
    
    def _get_huggingface_response(self, prompt: str) -> str:
        """Get response from Hugging Face Inference API (Free)"""
        api_key = os.getenv('HUGGINGFACE_API_KEY')
        api_url = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2"
        
        headers = {"Authorization": f"Bearer {api_key}"}
        
        financial_prompt = f"""You are a helpful financial advisor specializing in Indian finance. Provide clear, practical advice.

Question: {prompt}

Answer:"""
        
        payload = {
            "inputs": financial_prompt,
            "parameters": {
                "max_new_tokens": 500,
                "temperature": 0.7,
                "top_p": 0.95
            }
        }
        
        response = requests.post(api_url, headers=headers, json=payload, timeout=60)
        
        if response.status_code == 200:
            result = response.json()
            if isinstance(result, list) and len(result) > 0:
                return result[0].get('generated_text', '').replace(financial_prompt, '').strip()
        
        raise Exception(f"API error: {response.status_code}")
    
    def _get_ollama_response(self, prompt: str) -> str:
        """Get response from Ollama (Free, Local)"""
        api_url = "http://localhost:11434/api/generate"
        
        financial_prompt = f"You are a financial advisor for India. Provide practical advice in Indian context.\n\n{prompt}"
        
        payload = {
            "model": "llama2",
            "prompt": financial_prompt,
            "stream": False,
            "options": {
                "temperature": 0.7
            }
        }
        
        response = requests.post(api_url, json=payload, timeout=60)
        
        if response.status_code == 200:
            result = response.json()
            return result.get('response', '').strip()
        
        raise Exception(f"Ollama error: {response.status_code}")
    
    def _get_fallback_response(self, question: str) -> str:
        """Provide rule-based responses - India specific"""
        
        q = question.lower()
        
        # Budget keywords
        if any(w in q for w in ['budget', 'spend', 'monthly expense']):
            return """📊 Smart Budgeting Tips for India:

**50/30/20 Rule:**
- 50% - Needs (rent, groceries, EMIs, utilities)
- 30% - Wants (dining, entertainment, shopping)
- 20% - Savings & Investments (PPF, SIP, emergency fund)

**Average Indian Budget:**
- Housing: 25-30%
- Food: 20-25%
- Transport: 10-15%
- Healthcare: 5-10%
- Education: 5-10%
- Savings: 20% minimum

**Track Everything:**
Use apps like Money Manager, Walnut, or ET Money. Review monthly and adjust categories based on actual spending.

**Emergency Fund:** Build 6-12 months of expenses in liquid funds or savings account."""

        # Investment keywords
        elif any(w in q for w in ['invest', 'sip', 'mutual fund', 'stock']):
            return """📈 Investment Guide for India:

**For Beginners (Start with ₹500/month):**
1. **Index Funds/ETFs**: Nifty 50, Sensex (lowest cost, 12-15% returns)
2. **Large Cap Mutual Funds**: Safer, 10-12% returns
3. **ELSS**: Tax saving + equity growth (Section 80C benefit)

**Risk-Based Allocation:**
- Conservative: 60% Debt, 30% Large Cap, 10% Gold
- Moderate: 50% Equity, 30% Debt, 20% Hybrid
- Aggressive: 70% Equity, 20% Mid/Small Cap, 10% Debt

**Top Platforms:**
Zerodha, Groww, ETMoney, Paytm Money

**Key Principles:**
✓ Start SIP, don't wait for market timing
✓ Stay invested 5+ years
✓ Diversify across sectors
✓ Review quarterly, don't panic sell

**Tax:** LTCG >₹1L at 10%, STCG at 15%"""

        # Tax keywords
        elif any(w in q for w in ['tax', '80c', 'itr', 'deduction']):
            return """💰 Indian Tax Saving Guide (FY 2024-25):

**Section 80C (Max ₹1.5L):**
- EPF/VPF contributions
- ELSS mutual funds (3-year lock-in)
- PPF (7.1% tax-free, 15-year lock-in)
- Life insurance premiums
- NSC, Sukanya Samriddhi
- Home loan principal repayment
- Tuition fees

**Section 80D (Health Insurance):**
- Self & family: ₹25,000
- Parents (< 60): ₹25,000
- Parents (> 60): ₹50,000
- Max total: ₹75,000

**Section 80CCD(1B):**
- NPS contribution: Extra ₹50,000 deduction
- Total tax saving: ₹15,600!

**Section 80E:** Education loan interest (no limit)
**Section 80G:** Donations (50% or 100% based on institution)

**New vs Old Regime:**
Compare both before filing. Old regime better if deductions > ₹2.5L"""

        # PPF/NPS keywords
        elif any(w in q for w in ['ppf', 'nps', 'epf', 'retirement']):
            return """🏦 Retirement Planning in India:

**EPF (Employee Provident Fund):**
- 12% employer + 12% employee contribution
- 8.25% interest (tax-free!)
- Excellent for salaried employees
- Withdraw at retirement or after 5 years

**VPF (Voluntary PF):**
- Contribute extra beyond 12%
- Same 8.25% tax-free returns
- Better than most debt funds
- 80C benefit up to ₹1.5L

**PPF (Public Provident Fund):**
- 7.1% interest (completely tax-free)
- 15-year lock-in, can extend
- Min ₹500, Max ₹1.5L per year
- EEE benefit (Exempt-Exempt-Exempt)
- Safest long-term option

**NPS (National Pension System):**
- Market-linked returns (10-12% historically)
- Extra ₹50K deduction (80CCD1B)
- 60% lump sum at retirement (tax-free)
- 40% annuity (taxable)
- Best for aggressive growth

**Target:** ₹2-3 crore corpus for retirement"""

        # Real estate keywords
        elif any(w in q for w in ['house', 'property', 'home loan', 'real estate']):
            return """🏠 Home Buying Guide for India:

**When to Buy:**
✓ Stable job (3+ years)
✓ 20-30% down payment ready
✓ EMI < 40% of monthly income
✓ Planning to stay 7+ years in same city

**Home Loan (8-9% interest):**
- Term: 15-20 years
- EMI should be comfortable
- Compare rates across banks

**Tax Benefits:**
- 80C: ₹1.5L (principal repayment)
- 24b: ₹2L (interest payment)
- First time: Extra ₹1.5L (80EEA)
- Total: Up to ₹5L deduction!

**Real Estate Investment:**
- Returns: 5-8% typically
- Rental yield: 2-4% only
- Better options: REITs (8-10% yield)
- Appreciation varies by location

**Recommendation:**
Buy to live in, not as investment. Equity/MF give better returns (12-15%) with more liquidity.

**Best Cities for ROI:**
Tier 2/3 cities, upcoming areas, near metro/IT hubs"""

        # Credit card/CIBIL keywords
        elif any(w in q for w in ['credit', 'cibil', 'score', 'card']):
            return """💳 Credit Score & Cards Guide for India:

**CIBIL Score Ranges:**
- 750-900: Excellent (easy loan approval)
- 700-749: Good
- 650-699: Average
- 600-649: Poor
- <600: Very Poor

**How to Improve:**
1. Pay full amount, never minimum due (35% impact)
2. Keep utilization <30% of limit (30% impact)
3. Don't close old cards (15% impact)
4. Avoid multiple loan applications (10% impact)
5. Mix of secured + unsecured credit (10% impact)

**Check FREE:**
CIBIL, Experian, Equifax, CRIF (once per year free)

**Best Credit Cards India 2024:**
- Cashback: Amazon Pay, Flipkart Axis
- Rewards: HDFC Regalia, Axis Magnus
- Premium: AmEx Platinum, HDFC Infinia
- Travel: Intermiles, Air India SBI
- Entry: SBI SimplyCLICK, ICICI Amazon Pay

**Pro Tips:**
✓ Pay before due date
✓ Set autopay for minimum (pay full manually)
✓ Never withdraw cash (high interest!)
✓ Use for rewards, pay in full
✓ Good score saves lakhs in loan interest!"""

        # Emergency fund keywords
        elif any(w in q for w in ['emergency', 'contingency', 'liquid']):
            return """🚨 Emergency Fund Essentials for India:

**How Much:**
- Minimum: 6 months expenses
- Ideal: 12 months expenses
- Freelancers: 18 months

**Calculate Your Need:**
Monthly expenses × 6-12 months
Example: ₹40,000/month × 6 = ₹2.4 lakhs minimum

**Where to Keep:**
1. **Savings Account** (3-4% interest)
   • Instant access
   • Keep 2-3 months worth

2. **Liquid Funds** (6-7% returns)
   • Redeem in 1 day
   • Keep 3-6 months worth
   • No TDS, no exit load

3. **Fixed Deposits** (6.5-7.5%)
   • 1-2 year tenure
   • Premature withdrawal allowed
   • Keep 3-6 months worth

**DON'T Use:**
❌ Stocks/Equity (volatile)
❌ Real estate (not liquid)
❌ Long lock-in products

**Build Gradually:**
Start with ₹10,000/month, reach target in 12-24 months.

**Use ONLY for:**
Job loss, medical emergency, urgent home repairs, unexpected major expenses"""

        # General financial advice
        else:
            return """🎯 I'm your Financial Assistant for India! I can help with:

**Money Management:**
- Budgeting & expense tracking (50/30/20 rule)
- Emergency fund planning (6-12 months expenses)
- Tax planning (80C, 80D, NPS deductions)

**Investments:**
- Mutual Funds (SIP, ELSS, Index Funds)
- Stocks (Nifty 50, blue chips)
- Retirement planning (EPF, PPF, NPS)
- Gold & Real Estate strategies

**Debt Management:**
- Home loans, car loans, personal loans
- Credit card optimization
- CIBIL score improvement

**Insurance:**
- Health insurance (₹5-10L minimum)
- Term life insurance
- Tax benefits (Section 80D)

**Current Indian Financial Data:**
- Repo Rate: 6.5%
- FD Rates: 6.5-7.5%
- PPF: 7.1% (tax-free)
- EPF: 8.25% (tax-free)
- Home Loans: 8-9%
- Inflation: 5-6%

Ask me anything about managing your finances in India! 🇮🇳"""