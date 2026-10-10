package com.ijtc.rubelapp

import org.junit.Test

import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun typeListRecords_parseAndSerializeEnglishBanglaTypes() {
        val records = listOf(
            "Customer,কাস্টমার",
            "Supplier,সাপ্লায়ার"
        ).mapNotNull(::parseTypeListRecord)

        assertEquals(
            listOf(
                SetupTypeRecord("Customer", "কাস্টমার", "কাস্টমার"),
                SetupTypeRecord("Supplier", "সাপ্লায়ার", "সাপ্লায়ার")
            ),
            records
        )
        assertEquals(
            listOf("Customer,কাস্টমার,কাস্টমার", "Supplier,সাপ্লায়ার,সাপ্লায়ার"),
            serializeTypeListRecords(records)
        )
        assertNull(parseTypeListRecord("Invalid row"))
    }

    @Test
    fun typeListRecords_parsesThreeColumnsAndMigratesLegacyRows() {
        assertEquals(
            SetupTypeRecord("Customer", "কাস্টমার", "Customer"),
            parseTypeListRecord("Customer,কাস্টমার,Customer", isBanglaSelected = false)
        )
        assertEquals(
            SetupTypeRecord("Supplier", "সাপ্লায়ার", "Supplier"),
            parseTypeListRecord("Supplier,সাপ্লায়ার", isBanglaSelected = false)
        )
        assertEquals(
            SetupTypeRecord("Supplier", "সাপ্লায়ার", "সাপ্লায়ার"),
            parseTypeListRecord("Supplier,সাপ্লায়ার,", isBanglaSelected = true)
        )
        assertEquals(
            SetupTypeRecord("Supplier", "সাপ্লায়ার", "Supplier"),
            parseTypeListRecord("Supplier,সাপ্লায়ার,", isBanglaSelected = false)
        )
        assertEquals(
            listOf(
                SetupTypeRecord("Customer", "কাস্টমার", "Customer"),
                SetupTypeRecord("Supplier", "সাপ্লায়ার", "Supplier")
            ),
            typeListRecordsForLanguage(
                listOf(
                    SetupTypeRecord("Customer", "কাস্টমার", "কাস্টমার"),
                    SetupTypeRecord("Supplier", "সাপ্লায়ার", "সাপ্লায়ার")
                ),
                isBanglaSelected = false
            )
        )
        assertFalse(
            typeListLanguageSelection(
                listOf(SetupTypeRecord("Customer", "কাস্টমার", "Customer"))
            ) ?: true
        )
        assertTrue(
            typeListLanguageSelection(
                listOf(SetupTypeRecord("Customer", "কাস্টমার", "কাস্টমার"))
            ) ?: false
        )
        assertNull(typeListLanguageSelection(emptyList()))
        assertNull(parseTypeListRecord("Customer,কাস্টমার,Customer,Extra"))
    }

    @Test
    fun typeListRecords_reordersRowsAndKeepsAllTypeColumnsTogether() {
        val records = listOf(
            SetupTypeRecord("Receive", "গ্রহণ", "গ্রহণ"),
            SetupTypeRecord("Pay", "প্রদান", "প্রদান"),
            SetupTypeRecord("Buy", "ক্রয়", "ক্রয়")
        )

        assertEquals(
            listOf(records[2], records[0], records[1]),
            reorderTypeRecords(records, 2, 0)
        )
        assertEquals(records, reorderTypeRecords(records, 1, 1))
        assertEquals(records, reorderTypeRecords(records, -1, 1))
    }

    @Test
    fun typeListOptions_usesStoredSelectedTypeColumnAndRemovesDuplicates() {
        val records = listOf(
            SetupTypeRecord("Customer", "কাস্টমার", "কাস্টমার"),
            SetupTypeRecord("Supplier", "সাপ্লায়ার", "সাপ্লায়ার"),
            SetupTypeRecord(" customer ", "ক্রেতা", "ক্রেতা"),
            SetupTypeRecord("Vendor", "বিক্রেতা", ""),
            SetupTypeRecord("", "", "")
        )

        assertEquals(
            listOf("কাস্টমার", "সাপ্লায়ার", "ক্রেতা"),
            typeListOptions(records)
        )
    }

    @Test
    fun entryListParser_recognizesTypeListTypesInLegacyRows() {
        val fields = parseEntryListFields(
            "ID-1,User A,Track,2026-10-06,Customer,Sector A,INV-1,Contact,10,Bank,Name,CB,Note,Total",
            allowedTypes = listOf("Customer")
        )

        assertEquals("Customer", fields[4])
        assertEquals("Sector A", fields[5])
        assertEquals("Note", fields[16])
        assertEquals("Total", fields[17])
    }

    @Test
    fun typeListValidation_requiresBothFieldsAndRejectsDuplicatesInEitherLanguage() {
        val records = listOf(SetupTypeRecord("Customer", "কাস্টমার", "Customer"))

        assertEquals(
            "Enter both English and Bangla types.",
            typeListValidationError("Supplier", "", records)
        )
        assertEquals(
            "Enter both English and Bangla types.",
            typeListValidationError("", "সাপ্লায়ার", records)
        )
        assertEquals(
            "This English type already exists.",
            typeListValidationError("customer", "ক্রেতা", records)
        )
        assertEquals(
            "This Bangla type already exists.",
            typeListValidationError("Client", "কাস্টমার", records)
        )
        assertNull(typeListValidationError("Supplier", "সাপ্লায়ার", records))
    }

    @Test
    fun typeListValidation_allowsEditingCurrentRecordButStillRejectsOtherDuplicates() {
        val records = listOf(
            SetupTypeRecord("Customer", "কাস্টমার", "Customer"),
            SetupTypeRecord("Supplier", "সাপ্লায়ার", "Supplier")
        )

        assertNull(typeListValidationError("CUSTOMER", "কাস্টমার", records, 0))
        assertEquals(
            "This English type already exists.",
            typeListValidationError("Customer", "ক্রেতা", records, 1)
        )
        assertEquals(
            "This Bangla type already exists.",
            typeListValidationError("Client", "কাস্টমার", records, 1)
        )
    }

    @Test
    fun bangladeshiMobileValidation_acceptsLocalAndInternationalFormats() {
        assertTrue(isValidBangladeshiMobile("01712345678"))
        assertTrue(isValidBangladeshiMobile("+8801712345678"))
        assertTrue(isValidBangladeshiMobile("8801712345678"))
        assertTrue(isValidBangladeshiMobile("  +8801712345678  "))
        assertTrue(isValidBangladeshiMobile("+88 0171-2345678"))
        assertTrue(isValidBangladeshiMobile("017 12345678"))
    }

    @Test
    fun bangladeshiMobileValidation_rejectsInvalidNumbers() {
        assertFalse(isValidBangladeshiMobile("123456"))
        assertFalse(isValidBangladeshiMobile("+12345678901"))
        assertFalse(isValidBangladeshiMobile("01123456789"))
    }

    @Test
    fun validationHelpers_trimWhitespaceAndKeepValidEmail() {
        assertEquals("abc123", sanitizeText("  abc123  "))
        assertEquals("John Doe", sanitizeText("  John\nDoe  "))
        assertTrue(isValidLoginCode("123456789012345"))
        assertTrue(isValidLoginCode("12345 67890 12345"))
        assertTrue(isValidEmail("test@example.com"))
        assertFalse(isValidEmail("invalid-email"))
    }

    @Test
    fun appUpdateCheck_detectsVersionCodeOrVersionNameChangesWithoutDowngrades() {
        assertTrue(isAppUpdateAvailable(2, "1.0", 3, "1.0"))
        assertTrue(isAppUpdateAvailable(2, "1.0", 2, "1.1"))
        assertFalse(isAppUpdateAvailable(2, "1.1", 2, "1.1"))
        assertFalse(isAppUpdateAvailable(2, "1.1", 1, "1.2"))
    }

    @Test
    fun questionListQuestions_keepSpreadsheetRowsAndQuestionAnswerText() {
        val questions = parseQuestionListQuestions(
            "প্রশ্ন এক=উত্তর এক\r\n\r\nপ্রশ্ন দুই=উত্তর দুই\r\n"
        )

        assertEquals(2, questions.size)
        assertEquals(2, questions[0].row)
        assertEquals("প্রশ্ন এক", questions[0].question)
        assertEquals("উত্তর এক", questions[0].answer)
        assertEquals(4, questions[1].row)
        assertEquals("প্রশ্ন দুই", questions[1].question)
        assertEquals("উত্তর দুই", questions[1].answer)
    }

    @Test
    fun entryListLines_preserveEntryTextAndEqualsSigns() {
        assertEquals(
            listOf("  First=Entry  ", "Second Entry"),
            parseEntryListLines("  First=Entry  \r\n\r\nSecond Entry\r\n")
        )
        assertEquals(
            entryListFieldLabels,
            entryListSchemaHeader.split(",")
        )
        val entries = listOf("entry-one,user@example.com", "entry-two,user2@example.com")
        assertEquals(
            listOf(entryListSchemaHeader) + entries,
            serializeEntryListLines(entries)
        )
        assertEquals(
            entries,
            parseEntryListLines(
                (listOf(entryListSchemaHeader) + entries + entryListSchemaHeader)
                    .joinToString("\r\n")
            )
        )
        assertEquals(
            listOf(entryListSchemaHeader) + entries,
            serializeEntryListLines(entries + entryListSchemaHeader)
        )
    }

    @Test
    fun entryItemTotal_sumsOnlyItemsWithMatchingEntryId() {
        assertEquals(
            "15.75",
            calculateEntryItemTotal(
                "EN-1",
                listOf(
                    "EN-1,Product A,pcs,2,3,6,note",
                    "EN-2,Product B,pcs,9,1,9,note",
                    "EN-1,Product C,pcs,4.75,2,9.75,note"
                )
            )
        )
        assertEquals("0", calculateEntryItemTotal("EN-3", emptyList()))
        assertNull(
            calculateEntryItemTotal(
                "EN-1",
                listOf("EN-1,Product A,pcs,2,3,not-a-number,note")
            )
        )
    }

    @Test
    fun enterpriseDataFiles_containsEveryFixedEnterpriseFile() {
        assertEquals(
            listOf(
                "BankList",
                "ContactList",
                "EntryList",
                "FileList",
                "ItemList",
                "ProductList",
                "RPList",
                "TypeList",
                "UnitList",
                "UserList"
            ),
            enterpriseDataFiles.map { it.first }
        )
        assertTrue(isEnterpriseDataFile("BankList"))
        assertFalse(isEnterpriseDataFile("../BankList"))
    }

    @Test
    fun userListForm_serializesAndParsesFieldsAndStoresSwitchState() {
        val fields = listOf(
            "user@example.com",
            "device-01",
            "01712345678",
            "Test User",
            "Manager",
            "Off",
            "Registration claim"
        )

        assertEquals(
            listOf(
                "usEMail",
                "usDevice",
                "usMobile",
                "usName",
                "usRole",
                "usSwitch",
                "usClaim"
            ),
            userListFieldLabels
        )
        assertEquals(
            listOf("Admin", "Manager", "Cashier", "User", "Marketing"),
            userListRoleOptions
        )
        assertEquals(
            listOf("", "", "", "", "", "On", ""),
            newUserListFields()
        )
        assertEquals(fields.joinToString(","), buildUserListLine(fields))
        val registrationFields = fields.toMutableList().apply {
            this[4] = ""
            this[5] = ""
        }
        assertEquals(
            registrationFields.joinToString(","),
            buildUserListLine(registrationFields)
        )
        assertEquals(fields, parseUserListFields(fields.joinToString(",")))
        assertEquals(
            listOf("user@example.com", "device-01", "", "", "", "", ""),
            parseUserListFields("user@example.com,device-01")
        )
        assertEquals(
            listOf(
                "user@example.com",
                "device-01",
                "01712345678",
                "",
                "Manager",
                "On",
                ""
            ),
            parseUserListFields("user@example.com,device-01,01712345678,Manager,On")
        )
        assertEquals(
            fields,
            normalizeUserListLines(
                listOf(userListSchemaHeader, fields.joinToString(","))
            ).map(::parseUserListFields).single()
        )
        assertEquals(
            listOf(userListSchemaHeader, fields.joinToString(",")),
            serializeUserListLines(listOf(fields.joinToString(",")))
        )
        assertEquals(
            listOf(
                "user@example.com,device-01,01712345678,,Manager,On,"
            ),
            normalizeUserListLines(
                listOf(
                    "usEMail,usDevice,usMobile,usRole,usSwitch",
                    "user@example.com,device-01,01712345678,Manager,On"
                )
            )
        )
        val previousRows = normalizeUserListLines(
            listOf(
                "usEMail,usDevice,usMobile,usName,usRole,usSwitch",
                "user@example.com,device-01,01712345678,Test User,Manager,On"
            )
        )
        assertEquals(
            listOf("user@example.com", "device-01", "01712345678", "Test User", "Manager", "On", ""),
            parseUserListFields(previousRows.single())
        )
        assertNull(buildUserListLine(fields.toMutableList().apply { this[0] = "invalid" }))
        assertNull(buildUserListLine(fields.toMutableList().apply { this[5] = "Active" }))
        (0..3).forEach { index ->
            assertNull(
                "UserList field ${userListFieldLabels[index]} must be required",
                buildUserListLine(fields.toMutableList().apply { this[index] = "" })
            )
        }
    }

    @Test
    fun userListClaimUpdatesOnlyMatchingEmailAndDeviceAndEscapesTextareaContent() {
        val lines = listOf(
            userListSchemaHeader,
            "user@example.com,device-01,01712345678,Test User,User,On,",
            "other@example.com,device-02,01712345678,Other User,User,On,"
        )
        val claim = "First line, with comma\nSecond line \\ done"

        val updated = updateUserListClaim(
            lines,
            "USER@example.com",
            "device-01",
            claim
        )

        assertNotNull(updated)
        assertEquals("user@example.com", parseUserListFields(updated!![0])[0])
        assertEquals(claim, parseUserListFields(updated[0])[6])
        assertEquals(lines[2], updated[1])
        assertNull(updateUserListClaim(lines, "user@example.com", "wrong-device", "claim"))
        assertNull(updateUserListClaim(lines, "user@example.com", "device-01", "  "))
    }

    @Test
    fun loginRequiresMatchingEmailDeviceAndEnabledUserListRow() {
        val lines = listOf(
            userListSchemaHeader,
            "user@example.com,device-01,01712345678,Test User,User,On",
            "disabled@example.com,device-02,01712345678,Disabled User,User,Off",
            "blank@example.com,device-03,01712345678,Pending User,,"
        )

        assertTrue(isUserLoginAllowed(lines, "USER@example.com", "device-01"))
        assertFalse(isUserLoginAllowed(lines, "user@example.com", "wrong-device"))
        assertFalse(isUserLoginAllowed(lines, "disabled@example.com", "device-02"))
        assertFalse(isUserLoginAllowed(lines, "blank@example.com", "device-03"))
        assertFalse(isUserLoginAllowed(lines, "", "device-01"))
    }

    @Test
    fun contactListForm_serializesAndParsesNineCommaSeparatedFields() {
        val fields = listOf(
            "CN-001",
            "Rahim Uddin",
            "কাস্টমার",
            "01712345678",
            "Dhaka",
            "250",
            "Regular",
            "Dhanmondi",
            "Call before delivery",
            "175"
        )
        val generatedId = formatEntryListId(java.util.Date(0))
        assertEquals(
            listOf(
                "cnID", "cnName", "cnType", "cnMobile", "cnAddress",
                "cnOBalance", "cnGroup", "cnArea", "cnNote",
                "cnLBalance"
            ),
            contactListFieldLabels
        )
        assertEquals(
            listOf("কাস্টমার", "সাপ্লায়ার", "উভয়", "অন্যান্য"),
            contactListTypeOptions
        )
        assertEquals(
            listOf(
                "কাস্টমার", "সাপ্লায়ার", "উভয়", "অন্যান্য",
                "এডমিন", "ম্যানেজার", "সেলসম্যান", "ক্যাশিয়ার"
            ),
            contactListAllTypeOptions
        )
        assertTrue(generatedId.matches(Regex("\\d{16}")))
        assertEquals(contactListFieldLabels.size, newContactListFields(generatedId).size)
        assertEquals(generatedId, newContactListFields(generatedId)[0])
        assertEquals(fields.joinToString(","), buildContactListLine(fields))
        assertEquals(fields, parseContactListFields(fields.joinToString(",")))
        assertEquals(
            listOf("CN-001", "Rahim Uddin", "", "", "", "", "", "", "", ""),
            parseContactListFields("CN-001,Rahim Uddin")
        )
        assertEquals(
            listOf(
                "CN-001", "Rahim Uddin", "কাস্টমার", "01712345678",
                "Dhaka", "250", "Regular", "Dhanmondi",
                "Call before delivery", ""
            ),
            parseContactListFields(
                "CN-001,Rahim Uddin,কাস্টমার,01712345678,Dhaka,250," +
                    "Regular,Dhanmondi,Call before delivery"
            )
        )
    }

    @Test
    fun itemListForm_serializesEntryIdAndMigratesLegacyColumns() {
        val fields = listOf(
            "EN-002",
            "Rice",
            "kg",
            "75",
            "2",
            "150",
            "Local stock"
        )
        assertEquals(
            listOf(
                "itID", "pdName", "itUnit",
                "itRate", "itQty", "itTotal", "itNote"
            ),
            itemListFieldLabels
        )
        assertEquals(
            listOf("itID", "itProduct", "itRate", "itQty", "itUnit", "itTotal", "itNote"),
            itemListFormFieldLabels
        )
        assertEquals(itemListFieldLabels.size, newItemListFields("search item").size)
        assertEquals("search item", newItemListFields("search item")[0])
        assertEquals(List(itemListFieldLabels.size) { "" }, newItemListFields())
        assertEquals(fields.joinToString(","), buildItemListLine(fields))
        assertEquals(fields, parseItemListFields(fields.joinToString(",")))
        val (remainingItems, itemsRemoved) = removeItemListRowsForEntryId(
            listOf(
                "EN-001,Rice,kg,10,2,20,note",
                "EN-002,Tea,bag,5,3,15,",
                "EN-001,Sugar,kg,4,1,4,"
            ),
            "EN-001"
        )
        assertTrue(itemsRemoved)
        assertEquals(listOf("EN-002,Tea,bag,5,3,15,"), remainingItems)
        val (unchangedItems, noItemsRemoved) = removeItemListRowsForEntryId(
            listOf("EN-001,Rice,kg,10,2,20,note"),
            ""
        )
        assertFalse(noItemsRemoved)
        assertEquals(listOf("EN-001,Rice,kg,10,2,20,note"), unchangedItems)
        assertEquals(
            listOf("GID-001", "Rice", "kg", "75", "2", "150", "Local stock"),
            parseItemListFields("IT-001,GID-001,PD-001,Rice,kg,75,2,150,Local stock")
        )
        assertEquals(
            listOf("GID-001", "Rice", "kg", "75", "2", "150", "Local stock"),
            parseItemListFields("IT-001,GID-001,Rice,kg,75,2,150,Local stock")
        )
        assertEquals(
            listOf("GID-001,Rice,kg,75,2,150,Local stock"),
            normalizeItemListLines(
                listOf("IT-001,GID-001,PD-001,Rice,kg,75,2,150,Local stock")
            )
        )
        assertEquals(listOf("IT-001", "", "Rice", "", "", "", ""),
            parseItemListFields("IT-001,,Rice"))
        assertNull(buildItemListLine(fields.toMutableList().apply { this[1] = "" }))
        assertNull(buildItemListLine(fields.toMutableList().apply { this[0] = "" }))
        assertNull(buildItemListLine(fields.toMutableList().apply { this[2] = "bag,box" }))
    }

    @Test
    fun entryListContactPicker_usesDistinctNonBlankContactNames() {
        assertEquals(
            listOf("Rahim Uddin", "Karim"),
            parseEntryContactNames(
                listOf(
                    "CN-001, Rahim Uddin,কাস্টমার,01712345678",
                    "CN-002,Karim,সাপ্লায়ার,01812345678",
                    "CN-003,Rahim Uddin,উভয়,01912345678",
                    "CN-004,,অন্যান্য,",
                    "CN-005"
                )
            )
        )
    }

    @Test
    fun entryListCashierPicker_usesOnlyContactsWithStaffTypes() {
        assertEquals(
            listOf("Admin One", "Manager", "Salesman", "Cashier"),
            parseEntryCashierNames(
                listOf(
                    "CN-001,Admin One,এডমিন",
                    "CN-002,Manager,ম্যানেজার",
                    "CN-003,Salesman,সেলসম্যান",
                    "CN-004,Cashier,ক্যাশিয়ার",
                    "CN-005,Customer,কাস্টমার",
                    "CN-006,admin one,এডমিন",
                    "CN-007,,ক্যাশিয়ার"
                )
            )
        )
    }

    @Test
    fun contactListForm_rejectsMissingOrCommaSeparatedFieldValues() {
        val fields = listOf(
            "CN-001", "Rahim Uddin", "কাস্টমার", "01712345678",
            "", "", "", "", "", "", ""
        )

        assertNull(buildContactListLine(fields.toMutableList().apply { this[1] = "" }))
        assertNull(buildContactListLine(fields.toMutableList().apply { this[2] = "ভুল" }))
        assertNull(buildContactListLine(fields.toMutableList().apply { this[3] = "017,123" }))
        assertNull(buildContactListLine(fields.toMutableList().apply { this[3] = "0171\n2345678" }))
    }

    @Test
    fun contactListGroupAndAreaPickers_returnDistinctValuesFromTheirOwnColumns() {
        val contacts = listOf(
            "ID-1,Name 1,কাস্টমার,0171,Address,0,Regular,Dhanmondi,Note,0,0",
            "ID-2,Name 2,সাপ্লায়ার,0172,Address,0,VIP,Uttara,Note,0,0",
            "ID-3,Name 3,উভয়,0173,Address,0,regular,Mirpur,Note,0,0",
            "ID-4,Name 4,অন্যান্য,0174,Address,0,,dhanmondi,Note,0,0"
        )

        assertEquals(
            listOf("Regular", "VIP"),
            contactListColumnOptions(contacts, 6)
        )
        assertEquals(
            listOf("Dhanmondi", "Uttara", "Mirpur"),
            contactListColumnOptions(contacts, 7)
        )
        assertTrue(contactListColumnOptions(contacts, 8).isEmpty())
    }

    @Test
    fun bankListForm_serializesAndParsesSevenCommaSeparatedFields() {
        val fields = listOf(
            "2610071234567890",
            "Dutch Bangla Bank",
            "1234567890",
            "01712345678",
            "Dhanmondi",
            "1000",
            "500"
        )

        assertEquals(
            listOf(
                "bnID", "bnName", "bnAccount", "bnContact",
                "bnBranch", "bnOBalance", "bnLBalance"
            ),
            bankListFieldLabels
        )
        assertEquals(fields.joinToString(","), buildBankListLine(fields))
        assertEquals(fields, parseBankListFields(fields.joinToString(",")))
        assertEquals(
            listOf("ID-1", "Bank name", "", "", "", "", ""),
            parseBankListFields("ID-1,Bank name")
        )
    }

    @Test
    fun bankListForm_requiresIdAndNameAndRejectsSeparators() {
        val fields = listOf("ID-1", "Bank name", "", "", "", "", "")

        assertNull(buildBankListLine(fields.toMutableList().apply { this[0] = "" }))
        assertNull(buildBankListLine(fields.toMutableList().apply { this[1] = "" }))
        assertNull(
            buildBankListLine(fields.toMutableList().apply { this[2] = "123,456" })
        )
        assertNull(
            buildBankListLine(fields.toMutableList().apply { this[4] = "Branch\nName" })
        )
    }

    @Test
    fun productListForm_usesNewOrderAndMigratesLegacyRows() {
        val fields = listOf("2401010101010101", "Paracetamol", "2.50", "3.00", "25")

        assertEquals(
            listOf("pdID", "pdName", "pdBPrice", "pdSPrice", "pdOStock"),
            productListFieldLabels
        )
        assertEquals(fields.size, newProductListFields(fields[0]).size)
        assertEquals(fields[0], newProductListFields(fields[0])[0])
        assertEquals(fields.joinToString(","), buildProductListLine(fields))
        assertEquals(fields, parseProductListFields(fields.joinToString(",")))
        assertEquals(
            listOf("PD-001,Paracetamol,2.50,3.00,25"),
            normalizeProductListLines(
                listOf("PD-001,Tablet,Paracetamol,25,2.50,3.00")
            )
        )
        assertEquals(
            listOf("PD-003,Older product,1.00,2.00,10"),
            normalizeProductListLines(
                listOf("PD-003,Older product,10,1.00,2.00")
            )
        )
        assertEquals(
            listOf("PD-001,Paracetamol,2.50,3.00,25"),
            normalizeProductListLines(
                listOf(
                    "pdID,pdName,pdUnit,pdBPrice,pdSPrice,pdOStock",
                    "PD-001,Paracetamol,Tablet,2.50,3.00,25"
                )
            )
        )
        assertEquals(
            listOf("PD-003,Older product,1.00,2.00,10"),
            normalizeProductListLines(
                listOf(
                    "pdID,pdName,pdBPrice,pdSPrice,pdOStock",
                    "PD-003,Older product,1.00,2.00,10"
                )
            )
        )
        assertEquals(
            listOf(productListSchemaHeader, fields.joinToString(",")),
            serializeProductListLines(listOf(fields.joinToString(",")))
        )
        assertEquals(
            listOf(fields.joinToString(",")),
            normalizeProductListLines(
                listOf(productListSchemaHeader, fields.joinToString(","))
            )
        )
        assertEquals(
            listOf("Product name", "", "", "", ""),
            parseProductListFields("Product name")
        )
        val migratedWithoutId = normalizeProductListLines(
            listOf(
                "pdName,pdBPrice,pdSPrice,pdOStock",
                "Older product,1.00,2.00,10"
            )
        ).single().split(",")
        assertTrue(migratedWithoutId[0].matches(Regex("\\d{16}")))
        assertEquals(listOf("Older product", "1.00", "2.00", "10"), migratedWithoutId.drop(1))
        assertNull(buildProductListLine(fields.toMutableList().apply { this[0] = "" }))
        assertNull(buildProductListLine(fields.toMutableList().apply { this[1] = "" }))
        assertNull(buildProductListLine(fields.toMutableList().apply { this[2] = "1,000" }))
    }

    @Test
    fun itemProductPicker_usesDistinctProductNames() {
        val products = listOf(
            "PD-001,Paracetamol,2.50,3.00,25",
            "PD-002,Vitamin C,1.00,1.50,10",
            "PD-003,paracetamol,1.25,2.00,5",
            "PD-004,,0,0,0"
        )
        assertEquals(
            listOf("Paracetamol", "Vitamin C"),
            parseProductListNames(products)
        )
    }

    @Test
    fun itemListTotal_multipliesRateQuantityAndSelectedUnitValue() {
        val units = listOf("Box,12,Box (12)", "Piece,1,Piece (1)")

        assertEquals("1800", calculateItemListTotal("75", "2", "Box (12)", units))
        assertEquals("150.5", calculateItemListTotal("75.25", "2", "Piece (1)", units))
        assertEquals("0", calculateItemListTotal("0", "2", "Box (12)", units))
        assertEquals("150", calculateItemListTotal("75", "2", "", units))
        assertNull(calculateItemListTotal("", "2", "Box (12)", units))
        assertNull(calculateItemListTotal("75", "x", "Box (12)", units))
        assertNull(calculateItemListTotal("75", "2", "Unknown (4)", units))
        assertNull(calculateItemListTotal("75", "2", "Box (invalid)", units))
    }

    @Test
    fun itemUnitPicker_filtersOptionsByAnyCaseInsensitiveText() {
        val options = listOf("Box (12)", "Bottle (5)", "Box Large (24)")

        assertEquals(options, filterItemUnitOptions(options, ""))
        assertEquals(listOf("Bottle (5)"), filterItemUnitOptions(options, "ttle"))
        assertEquals(
            listOf("Box (12)", "Box Large (24)"),
            filterItemUnitOptions(options, "BOX")
        )
        assertEquals(emptyList<String>(), filterItemUnitOptions(options, "not found"))
    }

    @Test
    fun unitListForm_requiresFieldsAndPersistsReadOnlyDisplayValue() {
        val fields = listOf("Box", "12", "Box (12)", "4")

        assertEquals(
            listOf("utName", "utValue", "utUV", "utCount"),
            unitListFieldLabels
        )
        assertEquals(unitListFieldLabels.size, newUnitListFields().size)
        assertEquals(listOf("", "", "", "0"), newUnitListFields())
        assertEquals(fields.joinToString(","), buildUnitListLine(fields))
        assertEquals(fields, parseUnitListFields(fields.joinToString(",")))
        assertFalse(shouldShowUnitDeleteAction("Box,12,Box (12),3"))
        assertTrue(shouldShowUnitDeleteAction("Box,12,Box (12),0"))
        assertTrue(shouldShowUnitDeleteAction("Box,12,Box (12),"))
        assertEquals(
            listOf("Box", "12", "Box (12)", ""),
            parseUnitListFields("2401010101010101,2401010101010101,Box,Paracetamol,12")
        )
        assertEquals(
            listOf("Box", "12", "Box (12)", ""),
            parseUnitListFields("Box,Paracetamol,12")
        )
        assertEquals(listOf("Box", "12", "Box (12)", ""), parseUnitListFields("Box,12"))
        assertEquals(fields, parseUnitListFields("Box,12,Box (12),4"))
        val existingUnits = listOf("Box,12,Box (12)", "Bottle,5,Bottle (5)")
        assertTrue(isDuplicateUnitDisplayValue(existingUnits, "box (12)"))
        assertFalse(isDuplicateUnitDisplayValue(existingUnits, "Box (13)"))
        assertFalse(isDuplicateUnitDisplayValue(existingUnits, "Box (12)", excludedIndex = 0))
        assertEquals(
            "Box (12)",
            unitListDisplayValue("Box", "12")
        )
        assertEquals(
            listOf("Box", "Bottle"),
            parseUnitListNames(
                listOf(
                    "2401010101010101,2401010101010101,Box,Paracetamol,12",
                    "Bottle,Other,5",
                    "box,2,box (2)"
                )
            )
        )
        assertEquals(
            listOf("Box (12)", "Bottle (5)"),
            parseUnitListDisplayOptions(
                listOf("Box,12,Box (12)", "Bottle,5,Bottle (5)", "box,12,box (12)")
            )
        )
        assertEquals(
            listOf("Box,12,Box (12),"),
            normalizeUnitListLines(
                listOf("2401010101010101,2401010101010101,Box,Paracetamol,12")
            )
        )
        val items = listOf(
            "EN-01,Rice,Box (12),75,2,1800,",
            "EN-02,Beans,box (12),50,1,600,",
            "EN-03,Oil,Bottle (5),100,1,500,"
        )
        assertEquals(2, countUnitUsage(items, "Box (12)"))
        assertEquals(1, countUnitUsage(items, "Bottle (5)"))
        assertEquals(0, countUnitUsage(items, "Piece (1)"))
        assertEquals(
            listOf("Box,12,Box (12),2"),
            populateUnitListCounts(listOf("Box,12,Box (12),0"), items)
        )
        unitListFieldLabels.indices.forEach { index ->
            assertNull(buildUnitListLine(fields.toMutableList().apply { this[index] = "" }))
        }
        assertNull(buildUnitListLine(fields.toMutableList().apply { this[1] = "1,000" }))
        assertNull(buildUnitListLine(fields.toMutableList().apply { this[2] = "Wrong" }))
        assertNull(buildUnitListLine(fields.toMutableList().apply { this[3] = "many" }))
    }

    @Test
    fun fileListForm_usesFileIdAndMigratesLegacyRows() {
        val fields = listOf("2401010101010101", "Customer invoice", "Invoice.pdf")

        assertEquals(listOf("flID", "flNote", "flData"), fileListFieldLabels)
        assertEquals(fields.size, newFileListFields().size)
        assertEquals(listOf("", "", ""), newFileListFields())
        assertEquals(listOf("FILE-02", "", ""), newFileListFields("FILE-02"))
        assertEquals("flID,flNote,flData", fileListSchemaHeader)
        assertEquals(fields.joinToString(","), buildFileListLine(fields))
        assertEquals(fields, parseFileListFields(fields.joinToString(",")))
        val withoutId = listOf("", "Photo.jpg", "")
        assertNull(buildFileListLine(withoutId))
        assertEquals(
            listOf("FILE-OLD", "Receipt.jpg", "Customer receipt"),
            parseFileListFields("FILE-OLD,Receipt.jpg,Customer receipt")
        )
        assertEquals(
            listOf("", "", "FileList/FL-004/receipt.pdf"),
            normalizeFileListLines(
                listOf("FL-004,,FileList/FL-004/receipt.pdf")
            ).single().split(",")
        )
        assertEquals(
            listOf("GROUP-5", "", "FileList/FL-005/receipt.pdf"),
            normalizeFileListLines(
                listOf("FL-005,GROUP-5,FileList/FL-005/receipt.pdf")
            ).single().split(",")
        )
        assertEquals(
            listOf("GROUP-01", "Customer invoice", "Invoice.pdf"),
            normalizeFileListLines(
                listOf("FL-001,GROUP-01,Invoice.pdf,Customer invoice")
            ).single().split(",")
        )
        assertEquals(
            listOf("FILE-01", "Invoice.pdf", "Customer invoice"),
            parseFileListFields("FILE-01,Invoice.pdf,Customer invoice")
        )
        assertEquals(
            listOf("GROUP-01,Customer invoice,Invoice.pdf"),
            normalizeFileListLines(
                listOf("FL-001,GROUP-01,Invoice.pdf,Customer invoice")
            )
        )
        assertNull(buildFileListLine(fields.toMutableList().apply { this[0] = "" }))
        assertNull(buildFileListLine(fields.toMutableList().apply { this[2] = "" }))
        assertNull(buildFileListLine(fields.toMutableList().apply { this[2] = "Note,extra" }))
        val legacyLine = "FILE-OLD,Receipt.jpg,Customer receipt"
        val migratedLine = "FILE-OLD,Customer receipt,Receipt.jpg"
        assertEquals(
            listOf(migratedLine),
            normalizeFileListLines(listOf(legacyLine))
        )
        assertEquals(
            listOf("2401010101010101,,FileList/FL-006/receipt.pdf"),
            normalizeFileListLines(
                listOf("2401010101010101,FileList/FL-006/receipt.pdf")
            )
        )
        assertEquals(
            listOf(migratedLine),
            normalizeFileListLines(serializeFileListLines(listOf(migratedLine)))
        )
        assertEquals(
            listOf(fileListSchemaHeader, migratedLine),
            serializeFileListLines(listOf(migratedLine))
        )
        val (remaining, attachments) = removeFileListRowsForRecordId(
            listOf(
                fileListSchemaHeader,
                "CN-01,Receipt,FileList/FILE-01/receipt.pdf",
                "CN-02,Invoice,FileList/FILE-02/invoice.pdf",
                "CN-01,Note,"
            ),
            "CN-01"
        )
        assertEquals(
            listOf("CN-02,Invoice,FileList/FILE-02/invoice.pdf"),
            remaining
        )
        assertEquals(listOf("FileList/FILE-01/receipt.pdf"), attachments)
        val (remainingProducts, productAttachments) = removeFileListRowsForRecordId(
            listOf(
                fileListSchemaHeader,
                "PD-01,Label,FileData/FILE-10/label.pdf",
                "PD-02,Other,FileList/FILE-11/other.pdf"
            ),
            "PD-01"
        )
        assertEquals(
            listOf("PD-02,Other,FileList/FILE-11/other.pdf"),
            remainingProducts
        )
        assertEquals(listOf("FileData/FILE-10/label.pdf"), productAttachments)
        val (unchanged, noAttachments) = removeFileListRowsForRecordId(
            listOf(fileListSchemaHeader, ",Orphan,FileList/FILE-12/orphan.pdf"),
            ""
        )
        assertEquals(listOf(",Orphan,FileList/FILE-12/orphan.pdf"), unchanged)
        assertTrue(noAttachments.isEmpty())
    }

    @Test
    fun linkedFileSearchId_usesRecordIdForSupportedLists() {
        assertEquals("CN-01", linkedFileSearchId("ContactList", "CN-01,Name"))
        assertEquals("EN-02", linkedFileSearchId("EntryList", "EN-02,user"))
        assertEquals("BN-03", linkedFileSearchId("BankList", "BN-03,Bank"))
        assertEquals("PD-04", linkedFileSearchId("ProductList", "PD-04,Product"))
        assertEquals("", linkedFileSearchId("UnitList", "Unit,Value"))
    }

    @Test
    fun fileUploadError_explainsWhenEdgeFunctionNeedsDeployment() {
        assertTrue(fileUploadErrorMessage(405, "Method not allowed").contains("Deploy"))
        assertEquals(
            "The file is too large.",
            fileUploadErrorMessage(413, "The file is too large.")
        )
    }

    @Test
    fun enterpriseListDelete_allowsRemovingTheLastRow() {
        enterpriseDataFiles.forEach { (fileName, _) ->
            assertTrue("$fileName should allow removing the last row", canDeleteQuestionLine(fileName, 1))
            assertTrue("$fileName should allow deleting rows", canDeleteQuestionLine(fileName, 2))
        }
        assertFalse(canDeleteQuestionLine("QuestionList", 1))
        assertFalse(canDeleteQuestionLine("OnTest", 1))
    }

    @Test
    fun fileAttachmentPath_parsesFileDataAndLegacyFileListPaths() {
        assertEquals(
            FileAttachmentPath("FileData", "FL-006", "receipt.pdf"),
            parseFileAttachmentPath("FileData/FL-006/receipt.pdf")
        )
        assertEquals(
            FileAttachmentPath("FileList", "FL-006", "receipt.pdf"),
            parseFileAttachmentPath("FileList/FL-006/receipt.pdf")
        )
        assertNull(parseFileAttachmentPath("FileList/FL-006"))
        assertNull(parseFileAttachmentPath("OtherList/FL-006/receipt.pdf"))
        assertNull(parseFileAttachmentPath("FileList/../receipt.pdf"))
    }

    @Test
    fun entryListBankPicker_matchesContactAndReturnsDistinctBankNames() {
        val banks = listOf(
            "BN-1,Main Bank,001,Rahim Uddin",
            "BN-2,Second Bank,002,Karim",
            "BN-3,main bank,003,rahim uddin",
            "BN-4,Other Bank,004,Rahim Uddin"
        )

        assertEquals(
            listOf("Main Bank", "Other Bank"),
            entryBankNameOptions(banks, "Rahim Uddin")
        )
        assertEquals(emptyList<String>(), entryBankNameOptions(banks, "Unknown"))
        assertTrue(entryBankNameOptions(banks, "").isEmpty())
    }

    @Test
    fun bankListContactPicker_usesContactNamesAndPreservesUnknownExistingValue() {
        val contacts = listOf(
            "CN-001,Rahim Uddin,কাস্টমার",
            "CN-002,Karim,সাপ্লায়ার",
            "CN-003,Rahim Uddin,উভয়"
        )
        val existingBankContact = "Old contact"
        val options =
            (parseEntryContactNames(contacts) + existingBankContact).distinct()

        assertEquals(listOf("Rahim Uddin", "Karim", "Old contact"), options)
    }

    @Test
    fun entryListForm_serializesAndParsesEighteenCommaSeparatedFields() {
        val fields = listOf(
            "ID-1",
            "User A",
            "https://www.google.com/maps/search/?api=1&query=47.5951518%2C-122.3316393",
            "2026-10-06",
            "গ্রহণ",
            "Sector A",
            "INV-1",
            "01712345678",
            "500",
            "Bank A",
            "Bank Name A",
            "CB-1",
            "Discount A",
            "Labor A",
            "Rent A",
            "Cashier A",
            "Note A",
            "1500"
        )
        val line = buildEntryListLine(fields)

        assertEquals(fields.joinToString(","), line)
        assertEquals(fields, parseEntryListFields(line.orEmpty()))
        assertEquals(
            listOf("ID-1", "User A", "", "2026-10-06", "গ্রহণ") +
                List(13) { "" },
            parseEntryListFields("ID-1,User A,2026-10-06,গ্রহণ")
        )
    }

    @Test
    fun entryListItemAction_showsOnlyForSupportedEntryTypes() {
        val supportedTypes = listOf(
            "Buy", "Sell", "Adjust", "Return", "Damage",
            "ক্রয়", "বিক্রয়", "সমন্বয়", "ফেরত", "ড্যামেজ"
        )
        supportedTypes.forEach { entryType ->
            val fields = List(18) { "" }.toMutableList().apply {
                this[0] = "EN-01"
                this[4] = entryType
            }
            assertTrue(
                "Expected Item action for $entryType",
                shouldShowEntryItemAction(fields.joinToString(","))
            )
        }

        val unsupported = List(18) { "" }.toMutableList().apply {
            this[0] = "EN-02"
            this[4] = "গ্রহণ"
        }
        assertFalse(shouldShowEntryItemAction(unsupported.joinToString(",")))
        assertFalse(shouldShowEntryItemAction("EN-03,User"))
    }

    @Test
    fun entryListForm_formatsMapsTrackUrlAndParsesLegacyRows() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=47.5951518%2C-122.3316393",
            formatEntryTrackUrl(47.5951518, -122.3316393)
        )
        assertNull(formatEntryTrackUrl(91.0, 0.0))
        assertNull(formatEntryTrackUrl(0.0, Double.NaN))
        assertEquals(
            listOf(
                "ID-1", "User A", "", "2026-10-06", "গ্রহণ",
                "Sector A", "INV-1", "01712345678", "500",
                "Bank A", "Bank Name A", "CB-1", "", "", "", "",
                "Note A", "1500"
            ),
            parseEntryListFields(
                "ID-1,User A,2026-10-06,গ্রহণ,Sector A,INV-1,01712345678," +
                    "500,Bank A,Bank Name A,CB-1,Note A,1500"
            )
        )
        assertEquals(
            listOf(
                "ID-2", "User B", "track", "2026-10-06", "প্রদান",
                "Sector B", "INV-2", "Contact B", "750", "Bank B",
                "Bank Name B", "CB-2", "", "", "", "", "Note B", "2000"
            ),
            parseEntryListFields(
                "ID-2,User B,track,2026-10-06,প্রদান,Sector B,INV-2,Contact B," +
                    "750,Bank B,Bank Name B,CB-2,Note B,2000"
            )
        )
    }

    @Test
    fun entryListForm_hidesConditionalBankFields() {
        val fields = newEntryListFields(java.util.Date(0)).toMutableList()

        assertFalse(isEntryFieldVisible("enBank", fields))
        assertFalse(isEntryFieldVisible("enBName", fields))

        fields[7] = "Rahim"
        assertTrue(isEntryFieldVisible("enBank", fields))

        fields[9] = "1000"
        assertTrue(isEntryFieldVisible("enBName", fields))
        fields[9] = "0"
        assertTrue(isEntryFieldVisible("enBName", fields))
        fields[9] = ""
        assertFalse(isEntryFieldVisible("enBName", fields))
    }

    @Test
    fun entryListForm_calculatesCashBankTotal() {
        assertEquals("0", calculateEntryCashBankTotal("", ""))
        assertEquals("125.75", calculateEntryCashBankTotal("100.5", "25.25"))
        assertEquals("1000", calculateEntryCashBankTotal("1000", "0"))
        assertNull(calculateEntryCashBankTotal("invalid", "10"))
    }

    @Test
    fun entryListSectorPicker_suggestsUniqueSectorsForMatchingEntryType() {
        val entries = listOf(
            "ID-1,User,track,2026-10-06,গ্রহণ,Sales,",
            "ID-2,User,track,2026-10-06,গ্রহণ,sales,",
            "ID-3,User,track,2026-10-06,প্রদান,Sales,",
            "ID-4,User,track,2026-10-06,গ্রহণ,Purchase,",
            "ID-old,User,2026-10-06,গ্রহণ,Legacy sector,INV,,0,,Bank,CB,Note,10"
        )

        assertEquals(
            listOf("Sales", "Purchase", "Legacy sector"),
            entryListSectorOptions(entries, "গ্রহণ")
        )
        assertEquals(
            listOf("Sales"),
            entryListSectorOptions(entries, "প্রদান")
        )
        assertTrue(entryListSectorOptions(entries, "").isEmpty())
    }

    @Test
    fun entryListForm_formatsGeneratedIdAndDate() {
        val date = java.util.Date(0)

        assertTrue(formatEntryListId(date).matches(Regex("\\d{16}")))
        assertTrue(formatEntryListDate(date).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        assertEquals(18, newEntryListFields(date).size)
        assertEquals(formatEntryListId(date), newEntryListFields(date)[0])
        assertEquals(
            "login@example.com",
            newEntryListFields(date, " login@example.com ")[1]
        )
        assertEquals(formatEntryListDate(date), newEntryListFields(date)[3])
        assertEquals(listOf("গ্রহণ", "প্রদান", "খরচ"), entryListTypeOptions)
    }

    @Test
    fun entryListForm_rejectsMissingFieldsOrValuesContainingSeparators() {
        val baseFields = listOf(
            "ID-1", "User A", "", "2026-10-06", "গ্রহণ",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
        )
        assertNull(buildEntryListLine(baseFields.toMutableList().apply { this[0] = "" }))
        assertNull(buildEntryListLine(baseFields.toMutableList().apply { this[1] = "User,A" }))
        assertNull(buildEntryListLine(baseFields.toMutableList().apply { this[4] = "অজানা" }))
        assertNull(buildEntryListLine(baseFields.toMutableList().apply { this[5] = "Sector,A" }))
        assertNull(
            buildEntryListLine(
                baseFields.toMutableList().apply { this[2] = "https://maps.example/?q=1,2" }
            )
        )
    }

    @Test
    fun entryListForm_acceptsEnTypeOptionsFromTypeList() {
        val fields = listOf(
            "ID-1", "User A", "", "2026-10-06", "Customer",
            "", "", "", "", "", "", "", "", "", "", "", "", ""
        ).toMutableList().apply { this[5] = "Sector A" }

        assertEquals(
            fields.joinToString(","),
            buildEntryListLine(fields, listOf("Customer", "Supplier"))
        )
        assertNull(buildEntryListLine(fields, listOf("Supplier")))
        assertEquals(
            listOf("Sector A"),
            entryListSectorOptions(
                listOf(fields.joinToString(",")),
                "Customer",
                listOf("Customer")
            )
        )
    }

    @Test
    fun entryListDisplayOrder_putsLastFileEntryFirstAndKeepsOriginalIndices() {
        assertEquals(
            listOf(2 to "Latest", 1 to "Middle", 0 to "Oldest"),
            entryListDisplayOrder(listOf("Oldest", "Middle", "Latest"))
        )
    }

    @Test
    fun onTestQuestions_usesOnlyTheFirstHundredQuestionListRows() {
        val questionListQuestions = parseQuestionListQuestions(
            (1..105).joinToString("\n") { "Question $it=Answer $it" }
        )

        val onTestQuestions = firstOnTestQuestions(questionListQuestions)

        assertEquals(100, onTestQuestions.size)
        assertEquals("Question 1", onTestQuestions.first().question)
        assertEquals("Question 100", onTestQuestions.last().question)
    }

    @Test
    fun questionListDuplicateCheck_rejectsMatchingQuestionAndAnswer() {
        val entries = listOf("What is RubelApp?=An exam app", "Another question=Another answer")

        assertTrue(
            containsDuplicateQuestionAnswer(entries, " What is RubelApp? ", "An exam app")
        )
        assertTrue(
            containsDuplicateQuestionAnswer(entries, "what is rubelapp?", "AN EXAM APP")
        )
        assertFalse(
            containsDuplicateQuestionAnswer(entries, "What is RubelApp?", "A different answer")
        )
        assertFalse(
            containsDuplicateQuestionAnswer(entries, "A new question", "An exam app")
        )
        assertFalse(
            containsDuplicateQuestionAnswer(
                entries,
                "What is RubelApp?",
                "An exam app",
                excludedIndex = 0
            )
        )
        assertTrue(
            containsDuplicateQuestionAnswer(
                entries + "What is RubelApp?=An exam app",
                "What is RubelApp?",
                "An exam app",
                excludedIndex = 0
            )
        )
    }

}
