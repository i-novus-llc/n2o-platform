package net.n2oapp.platform.seek;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class CompositePkEntity {

    @EmbeddedId
    private Id id;

    @Column
    private int someField;

    protected CompositePkEntity() {
    }

    public CompositePkEntity(Id id, int someField) {
        this.id = id;
        this.someField = someField;
    }

    public Id getId() {
        return id;
    }

    public int getSomeField() {
        return someField;
    }

    @Override
    public String toString() {
        return "CompositePkEntity{" +
                "id=" + id +
                '}';
    }

    @Embeddable
    public static class Id implements Serializable {

        @Column
        private int first;

        @Column
        private int second;

        protected Id() {
        }

        public Id(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public int getFirst() {
            return first;
        }

        public int getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id)) return false;
            Id id = (Id) o;
            return first == id.first && second == id.second;
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return "Id{" +
                    "getFirst=" + first +
                    ", getSecond=" + second +
                    '}';
        }

    }

}
