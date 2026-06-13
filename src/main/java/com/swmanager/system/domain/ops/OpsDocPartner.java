package com.swmanager.system.domain.ops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * 문서-협력업체 연결 (tb_ops_doc_partner) — ops-fault-support M2/FR-M2-4.
 * 복합키 (doc_id, partner_id, role_label) — 동일 업체 복수역할 허용.
 */
@Entity
@Table(name = "tb_ops_doc_partner")
@IdClass(OpsDocPartner.PK.class)
@Getter @Setter
public class OpsDocPartner {

    @Id @Column(name = "doc_id")     private Long docId;
    @Id @Column(name = "partner_id") private Long partnerId;
    @Id @Column(name = "role_label", length = 50) private String roleLabel = "";

    public static class PK implements Serializable {
        private Long docId;
        private Long partnerId;
        private String roleLabel = "";

        public PK() {}
        public PK(Long docId, Long partnerId, String roleLabel) {
            this.docId = docId; this.partnerId = partnerId; this.roleLabel = roleLabel;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK)) return false;
            PK pk = (PK) o;
            return Objects.equals(docId, pk.docId) && Objects.equals(partnerId, pk.partnerId)
                    && Objects.equals(roleLabel, pk.roleLabel);
        }
        @Override public int hashCode() { return Objects.hash(docId, partnerId, roleLabel); }
    }
}
