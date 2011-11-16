package com.qcadoo.mes.technologies.constants;

public enum TechnologyState {
    
    DRAFT("draft") {

        @Override
        public TechnologyState changeState(final String targetState) {
            if (targetState != null && "accepted".equalsIgnoreCase(targetState.trim())) {
                return ACCEPTED;
            } else {
                return DECLINED;
            }
        }
    },
    ACCEPTED("accepted") {

        @Override
        public TechnologyState changeState(final String targetState) {
            if (targetState.trim().isEmpty() || "outdated".equalsIgnoreCase(targetState.trim())) {
                return OUTDATED;
            }
            return this;
        }
    },
    DECLINED("declined") {

        @Override
        public TechnologyState changeState(final String targetState) {
            return this;
        }
    },
    OUTDATED("outdated") {

        @Override
        public TechnologyState changeState(final String targetState) {
            return this;
        }
    };

    private String stringValue;

    private TechnologyState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public abstract TechnologyState changeState(final String targetState);
}
