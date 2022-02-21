package io.emeraldpay.polkaj.types

import spock.lang.Specification

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class DotAmountFormatterSpec extends Specification {

    static DecimalFormat localeFormat = new DecimalFormat()
    static DecimalFormatSymbols localeSymbols = localeFormat.decimalFormatSymbols

    DotAmount amount1 = DotAmount.fromPlancks(5_123_456_789_000) // 5 dot
    DotAmount amount2 = DotAmount.fromPlancks(  123_456_789_000) // 123 Milli
    DotAmount amount3 = DotAmount.fromPlancks(   23_456_789_000)
    DotAmount amount4 = DotAmount.fromPlancks(    3_456_789_000)
    DotAmount amount5 = DotAmount.fromPlancks(      456_789_000) // 456 Micro
    DotAmount amount6 = DotAmount.fromPlancks(       56_789_000)
    DotAmount amount7 = DotAmount.fromPlancks(        6_789_000)
    DotAmount amount8 = DotAmount.fromPlancks(          789_000) // 789 Point
    DotAmount amount9 = DotAmount.fromPlancks(           89_000)

    // update test data to conform the current local.
    // the test data is using EN standard for format, like "1,000.00", but on other locales it may be "1 000,00"
    static String forLocale(String exp) {
        exp.toCharArray().collect { c ->
            if (c == ".") {
                return localeSymbols.decimalSeparator
            } else if (c == ",") {
                return localeSymbols.groupingSeparator
            } else {
                return c
            }
        }.join("")
    }

    def "Simple"() {
        setup:
        def fmt = DotAmountFormatter.newBuilder()
                .fullNumber()
                .exactString(" ")
                .fullUnit()
                .build()
        when:
        def act = fmt.format(amount1)
        then:
        act == "5123456789000 Planck"
    }

    def "Standard full"() {
        expect:
        DotAmount value = DotAmount.fromPlancks(amount)
        DotAmountFormatter.fullFormatter().format(value) == exp
        where:
        amount              | exp
        5_123_456_789_000   | "5123456789000 Planck"
               56_789_000   | "56789000 Planck"
    }

    def "Standard auto"() {
        expect:
        DotAmount value = DotAmount.fromPlancks(amount)
        DotAmountFormatter.autoFormatter().format(value) == exp
        where:
        amount              | exp
        5_123_456_789_000   | forLocale("512.35 Dot")
           23_456_789_000   | forLocale("2.35 Dot")
            3_456_789_000   | forLocale("345.68 Millidot")
               56_789_000   | forLocale("5.68 Millidot")
                6_789_000   | forLocale("678.90 Microdot")
                   89_000   | forLocale("8.90 Microdot")
                    9_000   | forLocale("9,000.00 Planck")
    }

    def "Standard short"() {
        expect:
        DotAmount value = DotAmount.fromPlancks(amount)
        DotAmountFormatter.autoShortFormatter().format(value) == exp
        where:
        amount              | exp
        5_123_456_789_000   | forLocale("512.35 DOT")
               56_789_000   | forLocale("5.68 mDOT")
                   89_000   | forLocale("8.90 uDOT")
                    9_000   | forLocale("9,000.00 Planck")
    }

    def "With group separator"() {
        setup:
        def fmt = DotAmountFormatter.newBuilder()
                .fullNumber("#,##0")
                .exactString(" ")
                .fullUnit()
                .build()
        when:
        def act = fmt.format(amount1)
        then:
        act == forLocale("5,123,456,789,000 Planck")
    }

    def "With decimal part"() {
        setup:
        def fmt = DotAmountFormatter.newBuilder()
                .usingUnit(Units.Dot)
                .fullNumber("#,##0.00")
                .exactString(" ")
                .fullUnit()
                .build()
        when:
        def act = fmt.format(amount1)
        then:
        act == forLocale("512.35 Dot")
    }

    def "Converted with decimal part"() {
        setup:
        def fmt = DotAmountFormatter.newBuilder()
                .usingUnit(Units.Millidot)
                .fullNumber("#,##0.00")
                .exactString(" ")
                .fullUnit()
                .build()
        when:
        def act = fmt.format(amount2)
        then:
        act == forLocale("12,345.68 Millidot")
    }

    def "Converted large with decimal part"() {
        setup:
        def fmt = DotAmountFormatter.newBuilder()
                .usingUnit(Units.Planck)
                .fullNumber("#,##0.00")
                .exactString(" ")
                .fullUnit()
                .build()
        when:
        def act = fmt.format(amount1)
        then:
        act == forLocale("5,123,456,789,000.00 Planck")
    }

    def "Using short unit"() {
        setup:
        def fmt = DotAmountFormatter.newBuilder()
                .usingUnit(Units.Millidot)
                .fullNumber("#,##0.000")
                .exactString(" ")
                .shortUnit()
                .build()
        when:
        def act = fmt.format(amount2)
        then:
        act == forLocale("12,345.679 mDOT")
    }

    def "Using auto unit"() {
        setup:
        def fmt = DotAmountFormatter.newBuilder()
                .usingMinimalUnit()
                .fullNumber("#,##0.00")
                .exactString(" ")
                .shortUnit()
                .build()
        when:
        def act = fmt.format(amount5)
        then:
        act == forLocale("45.68 mDOT")
    }

    def "Using auto with limit unit"() {
        setup:
        def fmt = DotAmountFormatter.newBuilder()
                .usingMinimalUnit(Units.Millidot)
                .fullNumber("#,##0.000000")
                .exactString(" ")
                .shortUnit()
                .build()
        when:
        def act = fmt.format(amount8)
        then:
        act == forLocale("0.078900 mDOT")
    }

    def "Gives null on null input"() {
        when:
        def act = DotAmountFormatter.newBuilder().build().format(null)
        then:
        act == null
    }
}
