package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.entity.DailyClosingEntity
import com.example.data.entity.DebtEntity
import com.example.data.entity.DirectorPaymentEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.TransactionEntity

object PrintAndExportUtil {

    /**
     * Prints an official receipt for a single civil status form transaction.
     */
    fun printTransactionReceipt(context: Context, tx: TransactionEntity, directorateName: String) {
        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #1e293b; direction: rtl; text-align: right; }
                    .header { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 12px; margin-bottom: 15px; }
                    .title { font-size: 18px; font-weight: bold; margin: 0; }
                    .subtitle { font-size: 14px; color: #475569; margin: 4px 0; }
                    .receipt-no { font-size: 16px; font-weight: bold; color: #0284c7; margin-top: 8px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                    th, td { border: 1px solid #cbd5e1; padding: 8px 10px; font-size: 13px; text-align: right; }
                    th { background-color: #f1f5f9; }
                    .total { font-size: 16px; font-weight: bold; background-color: #e2e8f0; }
                    .footer { margin-top: 25px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px dashed #cbd5e1; padding-top: 10px; }
                    .developer { font-size: 10px; color: #94a3b8; margin-top: 6px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">الجمهورية اليمنية</div>
                    <div class="subtitle">$directorateName</div>
                    <div class="subtitle">صندوق ومبيعات استمارات الأحوال المدنية</div>
                    <div class="receipt-no">إيصال استلام رقم: ${tx.receiptNumber}</div>
                </div>

                <table>
                    <tr>
                        <th style="width: 35%;">اسم المواطن</th>
                        <td>${tx.citizenName}</td>
                    </tr>
                    <tr>
                        <th>رقم الاستمارة</th>
                        <td><strong>${tx.formNumber.ifBlank { "-" }}</strong></td>
                    </tr>
                    <tr>
                        <th>رقم القيد</th>
                        <td><strong>${tx.recordNumber.ifBlank { "-" }}</strong></td>
                    </tr>
                    <tr>
                        <th>نوع المعاملة</th>
                        <td>استمارة ${tx.transactionType}</td>
                    </tr>
                    <tr>
                        <th>الجنس</th>
                        <td>${tx.gender}</td>
                    </tr>
                    <tr>
                        <th>صافي دخل الصندوق</th>
                        <td>${CurrencyUtil.formatRiyal(tx.fundShare)}</td>
                    </tr>
                    <tr>
                        <th>حق الإدارة</th>
                        <td>${CurrencyUtil.formatRiyal(tx.directorateShare)}</td>
                    </tr>
                    <tr>
                        <th>التاريخ الميلادي</th>
                        <td>${tx.gregorianDate}</td>
                    </tr>
                    <tr>
                        <th>التاريخ الهجري</th>
                        <td>${tx.hijriDate}</td>
                    </tr>
                    <tr>
                        <th>الوقت</th>
                        <td>${tx.timeString}</td>
                    </tr>
                    <tr>
                        <th>أمين الصندوق</th>
                        <td>${tx.createdByName}</td>
                    </tr>
                    <tr class="total">
                        <th>المبلغ الإجمالي المسدد</th>
                        <td>${CurrencyUtil.formatRiyal(tx.salePrice)}</td>
                    </tr>
                </table>

                <div class="footer">
                    <div>يعتبر هذا الإيصال سند قبض رسمي لاستمارة الأحوال المدنية</div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, html, "إيصال_${tx.receiptNumber}")
    }

    /**
     * Prints or exports full daily closing report
     */
    fun printDailyReport(
        context: Context,
        closing: DailyClosingEntity,
        transactions: List<TransactionEntity>,
        directorateName: String
    ) {
        val rows = transactions.mapIndexed { index, tx ->
            """
            <tr>
                <td>${index + 1}</td>
                <td><strong>${tx.formNumber.ifBlank { "-" }}</strong></td>
                <td><strong>${tx.recordNumber.ifBlank { "-" }}</strong></td>
                <td style="text-align: right; font-weight: bold;">${tx.citizenName}</td>
                <td>${tx.transactionType}</td>
                <td>${tx.gender}</td>
                <td>${CurrencyUtil.formatNumber(tx.salePrice)}</td>
                <td style="color: #059669; font-weight: bold;">${CurrencyUtil.formatNumber(tx.fundShare)}</td>
                <td style="color: #0f172a; font-weight: bold;">${CurrencyUtil.formatNumber(tx.directorateShare)}</td>
                <td>${tx.timeString}</td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: sans-serif; margin: 15px; color: #0f172a; direction: rtl; text-align: right; }
                    .header { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 8px; margin-bottom: 12px; }
                    .title { font-size: 18px; font-weight: bold; }
                    .subtitle { font-size: 13px; color: #475569; }
                    .meta { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 12px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 11px; }
                    th, td { border: 1px solid #cbd5e1; padding: 5px 6px; text-align: center; }
                    th { background-color: #f1f5f9; font-weight: bold; }
                    .summary-grid { width: 100%; border-collapse: collapse; margin-bottom: 15px; font-size: 12px; }
                    .summary-grid td { border: 1px solid #94a3b8; padding: 6px; text-align: right; }
                    .bg-light { background-color: #f8fafc; font-weight: bold; }
                    .footer { margin-top: 20px; font-size: 11px; text-align: center; border-top: 1px solid #cbd5e1; padding-top: 8px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">الجمهورية اليمنية - $directorateName</div>
                    <div class="subtitle">تقرير كشف استمارات ومبيعات الأحوال المدنية</div>
                    <div class="subtitle">التاريخ: ${closing.gregorianDate} م الموافق ${closing.hijriDate}</div>
                </div>

                <table class="summary-grid">
                    <tr>
                        <td class="bg-light">رصيد بداية اليوم:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.openingBalance)}</td>
                        <td class="bg-light">صافي دخل الصندوق اليوم:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.totalFundShare)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">إجمالي الخرج والمصروفات:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.totalExpenses)}</td>
                        <td class="bg-light">صافي حركة اليومية:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.netToday)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">الرصيد المتوقع بالصندوق:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.expectedBalance)}</td>
                        <td class="bg-light">الرصيد الفعلي الموجود:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.actualBalance)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">حالة الصندوق (المطابقة):</td>
                        <td colspan="3">${if (closing.difference == 0.0) "مطابق تماماً ✓" else if (closing.difference > 0) "زيادة (+${CurrencyUtil.formatRiyal(closing.difference)})" else "عجز (${CurrencyUtil.formatRiyal(closing.difference)})"}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">حق الدولة:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.totalStateShare)}</td>
                        <td class="bg-light">حصة الإدارة (المدير):</td>
                        <td>${CurrencyUtil.formatRiyal(closing.totalDirectorateShare)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">إجمالي عدد المعاملات:</td>
                        <td colspan="3">${closing.totalTransactions} معاملة (جديد: ${closing.newCount} | تجديد: ${closing.renewCount} | بدل فاقد: ${closing.lostCount} | بدل تالف: ${closing.damagedCount}) - (ذكور: ${closing.malesCount}، إناث: ${closing.femalesCount})</td>
                    </tr>
                </table>

                <div style="font-weight: bold; font-size: 13px; margin-top: 10px;">كشف الاستمارات والمواطنين المسجلين:</div>
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>رقم الاستمارة</th>
                            <th>رقم القيد</th>
                            <th>اسم المواطن</th>
                            <th>نوع المعاملة</th>
                            <th>الجنس</th>
                            <th>المبلغ</th>
                            <th>صافي الصندوق</th>
                            <th>حق الإدارة</th>
                            <th>الوقت</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>

                <div class="footer">
                    <div>أمين الصندوق: __________________ &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; مدير الإدارة: __________________</div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, html, "يومية_${closing.gregorianDate}")
    }

    /**
     * Prints an official account statement for the Director's share (كشف حساب حق الإدارة)
     */
    fun printDirectorStatement(
        context: Context,
        directorateName: String,
        payments: List<com.example.data.entity.DirectorPaymentEntity>,
        totalEarned: Double,
        totalPaid: Double,
        remaining: Double
    ) {
        val paymentRows = if (payments.isEmpty()) {
            """<tr><td colspan="5" style="text-align:center; padding:12px; color:#64748b;">لا توجد دفعات منصرفة حتى الآن</td></tr>"""
        } else {
            payments.mapIndexed { index, p ->
                """
                <tr>
                    <td>${index + 1}</td>
                    <td>${CurrencyUtil.formatRiyal(p.amount)}</td>
                    <td>${p.gregorianDate} م (${p.timeString})</td>
                    <td>${p.notes ?: "دفعة نقدية"}</td>
                    <td>${p.paidByName}</td>
                </tr>
                """.trimIndent()
            }.joinToString("\n")
        }

        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #0f172a; direction: rtl; text-align: right; }
                    .header { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 10px; margin-bottom: 15px; }
                    .title { font-size: 18px; font-weight: bold; }
                    .subtitle { font-size: 13px; color: #475569; margin: 3px 0; }
                    table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                    th, td { border: 1px solid #cbd5e1; padding: 7px 8px; text-align: center; font-size: 12px; }
                    th { background-color: #f1f5f9; font-weight: bold; }
                    .summary-box { display: flex; justify-content: space-between; border: 1px solid #cbd5e1; padding: 12px; margin-bottom: 15px; background: #f8fafc; border-radius: 8px; }
                    .summary-item { text-align: center; flex: 1; }
                    .summary-label { font-size: 11px; color: #475569; }
                    .summary-val { font-size: 16px; font-weight: bold; margin-top: 4px; }
                    .footer { margin-top: 30px; border-top: 1px dashed #94a3b8; padding-top: 12px; display: flex; justify-content: space-around; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">الجمهورية اليمنية - $directorateName</div>
                    <div class="subtitle">كشف حساب مستحقات حق الإدارة (المدير)</div>
                    <div class="subtitle">تاريخ التقرير: ${HijriDateUtil.getTodayGregorianString()} م</div>
                </div>

                <div class="summary-box">
                    <div class="summary-item">
                        <div class="summary-label">إجمالي المستحق (200 لكل استمارة جديد):</div>
                        <div class="summary-val" style="color:#0f172a;">${CurrencyUtil.formatRiyal(totalEarned)}</div>
                    </div>
                    <div class="summary-item">
                        <div class="summary-label">إجمالي ما تم تسليمه (المحاسب به):</div>
                        <div class="summary-val" style="color:#059669;">${CurrencyUtil.formatRiyal(totalPaid)}</div>
                    </div>
                    <div class="summary-item">
                        <div class="summary-label">المتبقي طرف الصندوق:</div>
                        <div class="summary-val" style="color:#dc2626;">${CurrencyUtil.formatRiyal(remaining)}</div>
                    </div>
                </div>

                <div style="font-weight: bold; font-size: 13px; margin-top: 15px;">سجل الدفعات والمبالغ المسلمة للمدير:</div>
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>المبلغ المسلم</th>
                            <th>التاريخ والوقت</th>
                            <th>البيان / ملاحظات</th>
                            <th>الموظف القائم بالصرف</th>
                        </tr>
                    </thead>
                    <tbody>
                        $paymentRows
                    </tbody>
                </table>

                <div class="footer">
                    <div>توقيع أمين الصندوق: __________________</div>
                    <div>توقيع مدير الإدارة المستلم: __________________</div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, html, "كشف_حساب_المدير_${HijriDateUtil.getTodayGregorianString()}")
    }

    /**
     * Prints an official payment voucher for director disbursement.
     */
    fun printDirectorPaymentVoucher(
        context: Context,
        payment: DirectorPaymentEntity,
        directorateName: String
    ) {
        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: sans-serif; margin: 25px; color: #0f172a; direction: rtl; text-align: right; }
                    .header { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 12px; margin-bottom: 20px; }
                    .title { font-size: 20px; font-weight: bold; margin: 0; }
                    .subtitle { font-size: 14px; color: #475569; margin: 4px 0; }
                    .voucher-title { font-size: 17px; font-weight: bold; color: #0284c7; margin-top: 10px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 18px; }
                    th, td { border: 1px solid #cbd5e1; padding: 10px 14px; font-size: 14px; text-align: right; }
                    th { background-color: #f1f5f9; width: 35%; }
                    .amount-row { font-size: 18px; font-weight: bold; background-color: #ecfdf5; color: #059669; }
                    .footer { margin-top: 40px; display: flex; justify-content: space-between; font-size: 13px; }
                    .sign-box { text-align: center; }
                    .dots { margin-top: 40px; border-bottom: 1px dashed #64748b; width: 180px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">الجمهورية اليمنية</div>
                    <div class="subtitle">$directorateName</div>
                    <div class="subtitle">صندوق ومبيعات استمارات الأحوال المدنية</div>
                    <div class="voucher-title">سند صرف مستحقات المدير (حق الإدارة)</div>
                </div>

                <table>
                    <tr>
                        <th>رقم السند</th>
                        <td><strong>#DP-${payment.id}</strong></td>
                    </tr>
                    <tr class="amount-row">
                        <th>المبلغ المصروف</th>
                        <td>${CurrencyUtil.formatRiyal(payment.amount)}</td>
                    </tr>
                    <tr>
                        <th>التاريخ والوقت</th>
                        <td>${payment.gregorianDate} م - ${payment.timeString}</td>
                    </tr>
                    <tr>
                        <th>البيان والملاحظات</th>
                        <td>${payment.notes ?: "صرف مستحقات الإدارة عن مبيعات استمارات جديد"}</td>
                    </tr>
                    <tr>
                        <th>أمين الصندوق المسلّم</th>
                        <td>${payment.paidByName}</td>
                    </tr>
                </table>

                <div class="footer">
                    <div class="sign-box">
                        <div>توقيع أمين الصندوق</div>
                        <div class="dots"></div>
                    </div>
                    <div class="sign-box">
                        <div>توقيع واستلام المدير</div>
                        <div class="dots"></div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, html, "سند_صرف_المدير_${payment.id}")
    }

    /**
     * Prints full debts register statement showing all debtors.
     */
    fun printDebtsStatement(
        context: Context,
        debts: List<DebtEntity>,
        directorateName: String
    ) {
        val activeDebts = debts.filter { it.status != "PAID" && it.remainingAmount > 0 }
        val totalDebtsAmount = activeDebts.sumOf { it.remainingAmount }

        val debtRows = if (activeDebts.isEmpty()) {
            """<tr><td colspan="6" style="text-align:center; padding:12px; color:#64748b;">لا توجد أي ديون مسجلة بذمة الغير</td></tr>"""
        } else {
            activeDebts.mapIndexed { index, d ->
                """
                <tr>
                    <td>${index + 1}</td>
                    <td style="font-weight:bold;">${d.personName}</td>
                    <td style="color:#dc2626; font-weight:bold;">${CurrencyUtil.formatRiyal(d.remainingAmount)}</td>
                    <td>${CurrencyUtil.formatRiyal(d.originalAmount)}</td>
                    <td>${d.reason.ifBlank { "-" }}</td>
                    <td>${d.gregorianDate}</td>
                </tr>
                """.trimIndent()
            }.joinToString("\n")
        }

        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #0f172a; direction: rtl; text-align: right; }
                    .header { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 10px; margin-bottom: 15px; }
                    .title { font-size: 18px; font-weight: bold; }
                    .subtitle { font-size: 13px; color: #475569; margin: 3px 0; }
                    table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                    th, td { border: 1px solid #cbd5e1; padding: 7px 8px; text-align: center; font-size: 12px; }
                    th { background-color: #f1f5f9; font-weight: bold; }
                    .total-box { background: #fee2e2; border: 1px solid #fca5a5; padding: 12px; border-radius: 8px; margin-bottom: 15px; text-align: center; }
                    .total-label { font-size: 13px; color: #991b1b; }
                    .total-amount { font-size: 20px; font-weight: bold; color: #dc2626; margin-top: 4px; }
                    .footer { margin-top: 25px; border-top: 1px dashed #cbd5e1; padding-top: 10px; text-align: center; font-size: 11px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">الجمهورية اليمنية - $directorateName</div>
                    <div class="subtitle">كشف الديون والمبالغ الآجلة المسجلة طرف الصندوق</div>
                    <div class="subtitle">تاريخ التقرير: ${HijriDateUtil.getTodayGregorianString()} م</div>
                </div>

                <div class="total-box">
                    <div class="total-label">إجمالي الديون القائمة غير المسددة:</div>
                    <div class="total-amount">${CurrencyUtil.formatRiyal(totalDebtsAmount)}</div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>اسم المدين</th>
                            <th>المبلغ المتبقي</th>
                            <th>المبلغ الأصلي</th>
                            <th>السبب / الغرض</th>
                            <th>تاريخ الدين</th>
                        </tr>
                    </thead>
                    <tbody>
                        $debtRows
                    </tbody>
                </table>

                <div class="footer">
                    <div>صادر عن صندوق الأحوال المدنية والسجل المدني</div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, html, "كشف_الديون_${HijriDateUtil.getTodayGregorianString()}")
    }

    /**
     * Prints a custom period report (weekly, annual, all days, or custom date range).
     */
    fun printCustomPeriodReport(
        context: Context,
        periodTitle: String,
        periodSubtitle: String,
        transactions: List<TransactionEntity>,
        expenses: List<ExpenseEntity>,
        directorateName: String
    ) {
        val activeTxs = transactions.filter { it.status == "ACTIVE" }
        val activeExpenses = expenses.filter { it.status == "ACTIVE" }

        val totalSales = activeTxs.sumOf { it.salePrice }
        val totalState = activeTxs.sumOf { it.stateShare }
        val totalDirectorate = activeTxs.sumOf { it.directorateShare }
        val totalFund = activeTxs.sumOf { it.fundShare }
        val totalExpensesAmount = activeExpenses.sumOf { it.amount }
        val netBalance = totalSales - totalExpensesAmount

        val newCount = activeTxs.count { it.transactionType == "جديد" }
        val renewCount = activeTxs.count { it.transactionType == "تجديد" }
        val lostCount = activeTxs.count { it.transactionType == "بدل فاقد" }
        val damagedCount = activeTxs.count { it.transactionType == "بدل تالف" }

        val txRows = activeTxs.take(200).mapIndexed { index, tx ->
            """
            <tr>
                <td>${index + 1}</td>
                <td>${tx.formNumber.ifBlank { "-" }}</td>
                <td style="text-align: right; font-weight: bold;">${tx.citizenName}</td>
                <td>${tx.transactionType}</td>
                <td>${CurrencyUtil.formatNumber(tx.salePrice)}</td>
                <td style="color: #059669; font-weight: bold;">${CurrencyUtil.formatNumber(tx.fundShare)}</td>
                <td>${tx.gregorianDate}</td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: sans-serif; margin: 15px; color: #0f172a; direction: rtl; text-align: right; }
                    .header { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 8px; margin-bottom: 12px; }
                    .title { font-size: 18px; font-weight: bold; }
                    .subtitle { font-size: 13px; color: #475569; }
                    table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 11px; }
                    th, td { border: 1px solid #cbd5e1; padding: 5px 6px; text-align: center; }
                    th { background-color: #f1f5f9; font-weight: bold; }
                    .summary-grid { width: 100%; border-collapse: collapse; margin-bottom: 15px; font-size: 12px; }
                    .summary-grid td { border: 1px solid #94a3b8; padding: 6px; text-align: right; }
                    .bg-light { background-color: #f8fafc; font-weight: bold; }
                    .footer { margin-top: 20px; font-size: 11px; text-align: center; border-top: 1px solid #cbd5e1; padding-top: 8px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">الجمهورية اليمنية - $directorateName</div>
                    <div class="subtitle">$periodTitle</div>
                    <div class="subtitle">$periodSubtitle</div>
                </div>

                <table class="summary-grid">
                    <tr>
                        <td class="bg-light">إجمالي مبيعات الاستمارات:</td>
                        <td>${CurrencyUtil.formatRiyal(totalSales)}</td>
                        <td class="bg-light">إجمالي المصروفات (الخرج):</td>
                        <td>${CurrencyUtil.formatRiyal(totalExpensesAmount)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">صافي دخل الصندوق:</td>
                        <td>${CurrencyUtil.formatRiyal(totalFund)}</td>
                        <td class="bg-light">الصافي العام (المبيعات - الخرج):</td>
                        <td style="font-weight:bold; color: #059669;">${CurrencyUtil.formatRiyal(netBalance)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">حق الدولة:</td>
                        <td>${CurrencyUtil.formatRiyal(totalState)}</td>
                        <td class="bg-light">حصة الإدارة:</td>
                        <td>${CurrencyUtil.formatRiyal(totalDirectorate)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">عدد الاستمارات الإجمالي:</td>
                        <td colspan="3">${activeTxs.size} استمارة (جديد: $newCount | تجديد: $renewCount | بدل فاقد: $lostCount | بدل تالف: $damagedCount)</td>
                    </tr>
                </table>

                <div style="font-weight: bold; font-size: 13px; margin-top: 10px;">كشف تفصيلي بالعمليات:</div>
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>رقم الاستمارة</th>
                            <th>اسم المواطن</th>
                            <th>نوع المعاملة</th>
                            <th>المبلغ</th>
                            <th>صافي الصندوق</th>
                            <th>التاريخ</th>
                        </tr>
                    </thead>
                    <tbody>
                        $txRows
                    </tbody>
                </table>

                <div class="footer">
                    <div>أمين الصندوق: __________________ &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; مدير الإدارة: __________________</div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, html, "تقرير_مخصص_${HijriDateUtil.getTodayGregorianString()}")
    }

    /**
     * Prints the Official Yemeni Civil Status Daily Movement Sheet for Personal Application Forms
     * (كشف الحركة اليومي للاستمارات الشخصية) exactly matching the official Ministry of Interior paper template.
     */
    fun printOfficialMovementSheet(
        context: Context,
        formType: String, // "جديد", "تجديد", "بدل فاقد", "بدل تالف"
        gregorianDate: String,
        hijriDate: String,
        pageNumber: Int,
        totalPages: Int,
        pageTransactions: List<TransactionEntity>,
        directorateName: String,
        cashierName: String = "أمين الصندوق"
    ) {
        val pageSize = 50 // 25 rows on Right Side, 25 rows on Left Side
        val halfSize = 25

        // Right side items (first 25) and Left side items (next 25)
        val rightItems = pageTransactions.take(halfSize)
        val leftItems = pageTransactions.drop(halfSize).take(halfSize)

        val tableRows = StringBuilder()
        for (i in 0 until halfSize) {
            val rightIndex = (pageNumber - 1) * pageSize + i + 1
            val rightTx = rightItems.getOrNull(i)

            val leftIndex = (pageNumber - 1) * pageSize + halfSize + i + 1
            val leftTx = leftItems.getOrNull(i)

            tableRows.append("""
                <tr>
                    <!-- Right Side Column Set (Exact order from scanned document) -->
                    <td class="col-record">${rightTx?.recordNumber?.ifBlank { "" } ?: ""}</td>
                    <td class="col-form">${rightTx?.formNumber?.ifBlank { "" } ?: ""}</td>
                    <td class="col-name">${rightTx?.citizenName ?: ""}</td>
                    <td class="col-sign">${if (rightTx != null) "${rightTx.timeString}" else ""}</td>
                    <td class="col-num">$rightIndex</td>

                    <!-- Left Side Column Set (Exact order from scanned document) -->
                    <td class="col-record">${leftTx?.recordNumber?.ifBlank { "" } ?: ""}</td>
                    <td class="col-form">${leftTx?.formNumber?.ifBlank { "" } ?: ""}</td>
                    <td class="col-name">${leftTx?.citizenName ?: ""}</td>
                    <td class="col-sign">${if (leftTx != null) "${leftTx.timeString}" else ""}</td>
                    <td class="col-num">$leftIndex</td>
                </tr>
            """.trimIndent())
        }

        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <title>كشف الحركة اليومي للاستمارات الشخصية</title>
                <style>
                    @page {
                        size: A4 portrait;
                        margin: 5mm;
                    }
                    * {
                        box-sizing: border-box;
                    }
                    @media print {
                        body, .sheet-container {
                            background-color: #edf7ed !important;
                            -webkit-print-color-adjust: exact !important;
                            print-color-adjust: exact !important;
                        }
                        .sheet-title-box {
                            background-color: #1b5e20 !important;
                            color: #ffffff !important;
                            -webkit-print-color-adjust: exact !important;
                        }
                        table.official-table th {
                            background-color: #2e7d32 !important;
                            color: #ffffff !important;
                            -webkit-print-color-adjust: exact !important;
                        }
                    }
                    body {
                        font-family: "Amiri", "Traditional Arabic", "Segoe UI", Tahoma, sans-serif;
                        direction: rtl;
                        text-align: right;
                        margin: 0;
                        padding: 0;
                        color: #0f3817;
                        background: #edf7ed;
                        font-size: 10px;
                        line-height: 1.15;
                    }
                    .sheet-container {
                        width: 100%;
                        border: 3px double #1b5e20;
                        background: #edf7ed;
                        padding: 6px 8px;
                        min-height: 282mm;
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                    }
                    /* Official Top Header */
                    .official-header {
                        display: table;
                        width: 100%;
                        margin-bottom: 3px;
                    }
                    .header-right {
                        display: table-cell;
                        width: 33%;
                        vertical-align: top;
                        text-align: right;
                        font-weight: bold;
                        font-size: 11px;
                        color: #1b5e20;
                        line-height: 1.35;
                    }
                    .header-center {
                        display: table-cell;
                        width: 34%;
                        vertical-align: top;
                        text-align: center;
                    }
                    .header-left {
                        display: table-cell;
                        width: 33%;
                        vertical-align: top;
                        text-align: left;
                        direction: ltr;
                        font-size: 10px;
                    }
                    .header-left-inner {
                        direction: rtl;
                        text-align: right;
                        display: inline-block;
                        line-height: 1.35;
                        color: #1b5e20;
                    }
                    .basmala {
                        font-size: 12px;
                        font-weight: bold;
                        color: #1b5e20;
                        margin-bottom: 2px;
                    }
                    .sheet-title-box {
                        text-align: center;
                        margin: 3px 0 5px 0;
                        padding: 3px 6px;
                        background-color: #1b5e20;
                        color: #ffffff;
                        border: 1px solid #144a19;
                        border-radius: 3px;
                    }
                    .sheet-main-title {
                        font-size: 12.5px;
                        font-weight: bold;
                        letter-spacing: 0.3px;
                        color: #ffffff;
                    }
                    .type-highlight {
                        font-weight: 900;
                        background: #ffd54f;
                        color: #000000;
                        border-radius: 2px;
                        padding: 0 6px;
                    }
                    /* Official 2-Sided Table */
                    table.official-table {
                        width: 100%;
                        border-collapse: collapse;
                        border: 2px solid #1b5e20;
                        margin-top: 2px;
                    }
                    table.official-table th,
                    table.official-table td {
                        border: 1px solid #1b5e20;
                        padding: 1.5px 2px;
                        height: 20px;
                        vertical-align: middle;
                        text-align: center;
                    }
                    table.official-table th {
                        background-color: #2e7d32;
                        color: #ffffff;
                        font-weight: bold;
                        font-size: 9px;
                        line-height: 1.1;
                    }
                    table.official-table tr:nth-child(even) {
                        background-color: #ffffff;
                    }
                    table.official-table tr:nth-child(odd) {
                        background-color: #e4f4e5;
                    }
                    .col-num { width: 4%; font-weight: bold; font-size: 8.5px; color: #1b5e20; }
                    .col-record { width: 10.5%; font-size: 9px; font-weight: 600; }
                    .col-form { width: 10.5%; font-size: 9px; font-weight: 600; }
                    .col-name { width: 43%; text-align: right !important; padding-right: 4px !important; font-size: 9.5px; font-weight: 600; }
                    .col-sign { width: 28%; font-size: 8px; color: #2d5a32; }

                    /* Official Signatures Footer */
                    .official-footer {
                        margin-top: 8px;
                        padding-top: 4px;
                        display: table;
                        width: 100%;
                        border-top: 1px dashed #1b5e20;
                    }
                    .sign-col {
                        display: table-cell;
                        width: 33.33%;
                        text-align: center;
                        vertical-align: top;
                        font-size: 10px;
                        color: #1b5e20;
                        line-height: 1.5;
                    }
                    .sign-title {
                        font-weight: bold;
                        margin-bottom: 2px;
                    }
                        font-weight: bold;
                        margin-bottom: 2px;
                    }
                </style>
            </head>
            <body>
                <div class="sheet-container">
                    <div>
                        <!-- Header -->
                        <div class="official-header">
                            <div class="header-right">
                                <div style="font-size: 14px; font-weight: bold;">الجمهورية اليمنية</div>
                                <div style="font-size: 12px; font-weight: bold;">وزارة الداخلية</div>
                                <div style="font-size: 11px; font-weight: bold;">مصلحة الأحوال المدنية والسجل المدني</div>
                            </div>
                            <div class="header-center">
                                <div class="basmala">بسم الله الرحمن الرحيم</div>
                                <div style="margin: 2px 0;">
                                    <!-- Official Yemeni Eagle Emblem SVG -->
                                    <svg width="85" height="52" viewBox="0 0 120 80" xmlns="http://www.w3.org/2000/svg">
                                        <!-- Left Wing -->
                                        <path d="M60,42 C52,35 38,20 18,16 C12,15 8,18 10,22 C13,29 25,38 34,42 C24,43 14,46 12,50 C10,54 16,55 24,53 C33,51 44,48 55,47 Z" fill="#E5A93C" stroke="#B8860B" stroke-width="0.8"/>
                                        <path d="M58,40 C48,32 32,22 19,20 C24,27 34,35 44,40 C35,42 22,46 20,49 C26,50 38,48 50,46 Z" fill="#F6C358"/>
                                        <!-- Right Wing -->
                                        <path d="M60,42 C68,35 82,20 102,16 C108,15 112,18 110,22 C107,29 95,38 86,42 C96,43 106,46 108,50 C110,54 104,55 96,53 C87,51 76,48 65,47 Z" fill="#E5A93C" stroke="#B8860B" stroke-width="0.8"/>
                                        <path d="M62,40 C72,32 88,22 101,20 C96,27 86,35 76,40 C85,42 98,46 100,49 C94,50 82,48 70,46 Z" fill="#F6C358"/>
                                        <!-- Head & Beak -->
                                        <path d="M60,22 C56,18 57,10 60,6 C63,10 64,18 60,22 Z" fill="#F6C358" stroke="#B8860B" stroke-width="0.8"/>
                                        <polygon points="61,12 65,14 61,16" fill="#D97706"/>
                                        <circle cx="60" cy="12" r="1" fill="#000"/>
                                        <!-- Left Flag -->
                                        <line x1="38" y1="62" x2="82" y2="32" stroke="#B45309" stroke-width="1.5"/>
                                        <path d="M34,34 C30,32 25,36 22,35 L22,38 C26,39 30,36 34,38 Z" fill="#CE1126"/>
                                        <path d="M34,38 C30,36 26,39 22,38 L22,41 C26,42 30,39 34,41 Z" fill="#FFFFFF" stroke="#E2E8F0" stroke-width="0.2"/>
                                        <path d="M34,41 C30,39 26,42 22,41 L22,44 C26,45 30,42 34,44 Z" fill="#000000"/>
                                        <!-- Right Flag -->
                                        <line x1="82" y1="62" x2="38" y2="32" stroke="#B45309" stroke-width="1.5"/>
                                        <path d="M86,34 C90,32 95,36 98,35 L98,38 C94,39 90,36 86,38 Z" fill="#CE1126"/>
                                        <path d="M86,38 C90,36 94,39 98,38 L98,41 C94,42 90,39 86,41 Z" fill="#FFFFFF" stroke="#E2E8F0" stroke-width="0.2"/>
                                        <path d="M86,41 C90,39 94,42 98,41 L98,44 C94,45 90,42 86,44 Z" fill="#000000"/>
                                        <!-- Chest Shield -->
                                        <path d="M52,24 L68,24 C68,36 65,46 60,50 C55,46 52,36 52,24 Z" fill="#FFFFFF" stroke="#B8860B" stroke-width="1.2"/>
                                        <path d="M53,38 C56,36 64,36 67,38 L66,45 C63,48 60,49 54,45 Z" fill="#0284C7"/>
                                        <path d="M53,32 L67,32 L67,37 C64,36 56,36 53,37 Z" fill="#CA8A04"/>
                                        <path d="M57,26 C59,28 61,28 63,26 C63,30 57,30 57,26 Z" fill="#15803D"/>
                                        <circle cx="60" cy="29" r="1" fill="#DC2626"/>
                                        <!-- Scroll -->
                                        <path d="M44,56 C52,54 68,54 76,56 C74,60 68,61 60,61 C52,61 46,60 44,56 Z" fill="#FEF08A" stroke="#CA8A04" stroke-width="0.8"/>
                                    </svg>
                                </div>
                            </div>
                            <div class="header-left">
                                <div class="header-left-inner">
                                    <div style="display: flex; align-items: center; justify-content: flex-end; margin-bottom: 2px;">
                                        <!-- Civil Status Authority Circular Seal SVG -->
                                        <svg width="42" height="42" viewBox="0 0 70 70" xmlns="http://www.w3.org/2000/svg" style="margin-left: 8px;">
                                            <circle cx="35" cy="35" r="31" fill="#F0F9FF" stroke="#0284C7" stroke-width="2"/>
                                            <circle cx="35" cy="35" r="27" fill="none" stroke="#0369A1" stroke-width="1"/>
                                            <path d="M23,32 C28,30 33,30 35,33 C37,30 42,30 47,32 L47,42 C42,40 37,40 35,43 C33,40 28,40 23,42 Z" fill="#BAE6FD" stroke="#0284C7" stroke-width="0.8"/>
                                            <line x1="35" y1="18" x2="35" y2="32" stroke="#B45309" stroke-width="1.2"/>
                                            <line x1="27" y1="22" x2="43" y2="22" stroke="#B45309" stroke-width="1.2"/>
                                            <path d="M25,27 C27,29 29,29 31,27 Z" fill="#F59E0B"/>
                                            <path d="M39,27 C41,29 43,29 45,27 Z" fill="#F59E0B"/>
                                        </svg>
                                        <div>
                                            <div>الرقم : ....................</div>
                                            <div>التاريخ : <strong>$gregorianDate م</strong></div>
                                            <div>الموافق : <strong>$hijriDate</strong></div>
                                            <div>المرفقات : ....................</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Title -->
                        <div class="sheet-title-box">
                            <div class="sheet-main-title">
                                كشف الحركة اليومي للاستمارات الشخصية ( <span class="type-highlight">$formType</span> ) بمديـرية: <strong>$directorateName</strong>
                            </div>
                        </div>

                        <!-- Official Table -->
                        <table class="official-table">
                            <thead>
                                <tr>
                                    <!-- Right Half Column Headers (Exact scanned document) -->
                                    <th class="col-record">رقم القيد<br>التسلسلي</th>
                                    <th class="col-form">رقم<br>الاستمارة</th>
                                    <th class="col-name">الإســـــــــــ ـــــــــــم</th>
                                    <th class="col-sign">تاريخ القيد +<br>توقيع الفني المختص</th>
                                    <th class="col-num">م</th>

                                    <!-- Left Half Column Headers (Exact scanned document) -->
                                    <th class="col-record">رقم القيد<br>التسلسلي</th>
                                    <th class="col-form">رقم<br>الاستمارة</th>
                                    <th class="col-name">الإســـــــــــ ـــــــــــم</th>
                                    <th class="col-sign">تاريخ القيد +<br>توقيع الفني المختص</th>
                                    <th class="col-num">م</th>
                                </tr>
                            </thead>
                            <tbody>
                                $tableRows
                            </tbody>
                        </table>
                    </div>

                    <!-- Footer Signatures -->
                    <div class="official-footer">
                        <div class="sign-col">
                            <div class="sign-title">أمين الصندوق</div>
                            <div>الاسم / $cashierName</div>
                            <div>التوقيع / ....................................</div>
                        </div>
                        <div class="sign-col">
                            <div class="sign-title">الفني المختص باستلام البطائق وطباعتها</div>
                            <div>الاسم / ....................................</div>
                            <div>التوقيع / ....................................</div>
                        </div>
                        <div class="sign-col">
                            <div class="sign-title">مدير فرع الأحوال المدنية بمديرية $directorateName</div>
                            <div>الاسم / ....................................</div>
                            <div>التوقيع / ....................................</div>
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, html, "كشف_حركة_${formType}_ورقة_${pageNumber}_$gregorianDate")
    }

    private fun printHtml(context: Context, html: String, jobName: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter: PrintDocumentAdapter = webView.createPrintDocumentAdapter(jobName)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("res1", "default", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()

                printManager?.print(jobName, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html; charset=UTF-8", "UTF-8", null)
    }

    /**
     * Exports transactions or summary to CSV / plain text and triggers Android share sheet.
     */
    fun shareTextReport(context: Context, title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة التقرير عبر:"))
    }
}
