package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.entity.DailyClosingEntity
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
                        <th>نوع المعاملة</th>
                        <td>استمارة ${tx.transactionType}</td>
                    </tr>
                    <tr>
                        <th>الجنس</th>
                        <td>${tx.gender}</td>
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
                        <th>الموظف المستلم</th>
                        <td>${tx.createdByName}</td>
                    </tr>
                    <tr class="total">
                        <th>المبلغ المسدد</th>
                        <td>${CurrencyUtil.formatRiyal(tx.salePrice)}</td>
                    </tr>
                </table>

                <div class="footer">
                    <div>يعتبر هذا الإيصال سند قبض رسمي لاستمارة الأحوال المدنية</div>
                    <div class="developer">برمجة وتصميم محمد هشام الصلاحي</div>
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
                <td>${tx.receiptNumber}</td>
                <td>${tx.citizenName}</td>
                <td>${tx.transactionType}</td>
                <td>${tx.gender}</td>
                <td>${CurrencyUtil.formatNumber(tx.salePrice)}</td>
                <td>${CurrencyUtil.formatNumber(tx.stateShare)}</td>
                <td>${CurrencyUtil.formatNumber(tx.directorateShare)}</td>
                <td>${CurrencyUtil.formatNumber(tx.fundShare)}</td>
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
                    <div class="subtitle">تقرير يومية صندوق ومبيعات استمارات الأحوال المدنية</div>
                    <div class="subtitle">التاريخ: ${closing.gregorianDate} م الموافق ${closing.hijriDate}</div>
                </div>

                <table class="summary-grid">
                    <tr>
                        <td class="bg-light">رصيد بداية اليوم:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.openingBalance)}</td>
                        <td class="bg-light">إجمالي مبيعات الاستمارات:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.totalIncome - closing.otherIncome - closing.debtPaidAmount)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">إجمالي الخرج:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.totalExpenses)}</td>
                        <td class="bg-light">صافي حركة اليوم:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.netToday)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">الرصيد المتوقع:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.expectedBalance)}</td>
                        <td class="bg-light">الرصيد الفعلي:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.actualBalance)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">حالة الصندوق (الفرق):</td>
                        <td colspan="3">${if (closing.difference == 0.0) "مطابق ✓" else if (closing.difference > 0) "زيادة (+${CurrencyUtil.formatRiyal(closing.difference)})" else "عجز (${CurrencyUtil.formatRiyal(closing.difference)})"}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">حق الدولة:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.totalStateShare)}</td>
                        <td class="bg-light">حصة الإدارة:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.totalDirectorateShare)}</td>
                    </tr>
                    <tr>
                        <td class="bg-light">نصيب الصندوق:</td>
                        <td>${CurrencyUtil.formatRiyal(closing.totalFundShare)}</td>
                        <td class="bg-light">إجمالي المعاملات:</td>
                        <td>${closing.totalTransactions} (ذكور: ${closing.malesCount}، إناث: ${closing.femalesCount})</td>
                    </tr>
                </table>

                <div style="font-weight: bold; font-size: 13px; margin-top: 10px;">جدول العمليات التفصيلية:</div>
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>رقم العملية</th>
                            <th>اسم المواطن</th>
                            <th>النوع</th>
                            <th>الجنس</th>
                            <th>المبلغ</th>
                            <th>حق الدولة</th>
                            <th>حصة الإدارة</th>
                            <th>الصندوق</th>
                            <th>الوقت</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>

                <div class="footer">
                    <div>أمين الصندوق: ${closing.closedByName ?: "المسؤول"} &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; مدير الإدارة: __________________</div>
                    <div style="margin-top: 5px; color: #64748b;">برمجة وتصميم محمد هشام الصلاحي</div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, html, "يومية_${closing.gregorianDate}")
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
