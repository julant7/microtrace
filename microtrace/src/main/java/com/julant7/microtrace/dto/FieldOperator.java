package com.julant7.microtrace.dto;

public enum FieldOperator {
    EQ {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return true;
        }
    },
    NE {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return true;
        }
    },
    GT {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return fieldType == Field.FieldType.DOUBLE || fieldType == Field.FieldType.LONG
                    || fieldType == Field.FieldType.DATE;
        }
    },
    LT {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return fieldType == Field.FieldType.DOUBLE || fieldType == Field.FieldType.LONG
                    || fieldType == Field.FieldType.DATE;
        }
    },
    GTE {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return fieldType == Field.FieldType.DOUBLE || fieldType == Field.FieldType.LONG
                    || fieldType == Field.FieldType.DATE;
        }
    },
    LTE {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return fieldType == Field.FieldType.DOUBLE || fieldType == Field.FieldType.LONG
                    || fieldType == Field.FieldType.DATE;
        }
    },
    REGEXP {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return fieldType == Field.FieldType.KEYWORD || fieldType == Field.FieldType.TEXT;
        }
    },
    IN {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return fieldType == Field.FieldType.KEYWORD || fieldType == Field.FieldType.TEXT;
        }
    },
    NOT_IN {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return false;
        }
    },
    EXISTS {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return false;
        }
    },
    NOT_EXISTS {
        @Override
        public boolean supports(Field.FieldType fieldType) {
            return false;
        }
    };
    // TODO специальный оператор для многих временных меток


    public abstract boolean supports(Field.FieldType fieldType);

}
