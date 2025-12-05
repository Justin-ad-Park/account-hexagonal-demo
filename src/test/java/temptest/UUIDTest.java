package temptest;

import com.tngtech.archunit.thirdparty.com.google.common.base.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.fasterxml.uuid.Generators;
import com.github.f4b6a3.ulid.UlidCreator;
import com.github.ksuid.Ksuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UUIDTest {
    @Test
    void generateUUIDv7_and_compareSortOrder() {
        compareID(() -> Generators.timeBasedGenerator().generate().toString());
    }

    @Test
    void generateULID() {
        // 1) UUID 리스트 생성
        compareID(() -> UlidCreator.getUlid().toString());
    }

    @Test
    void generateKSUID() {
        compareID( () -> Ksuid.newKsuid().toString());
    }

    private static void compareID(Supplier<String> idSupplier) {
        // 1) UUID 리스트 생성
        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            uuids.add(idSupplier.get());
        }

        sortAndCompare(uuids);
    }


    private static void sortAndCompare(List<String> uuids) {
        // 2) 원본 리스트 복사
        List<String> sorted = new ArrayList<>(uuids);

        // 3) 문자열 기준 정렬
        Collections.sort(sorted);

        for (int i = 0; i < uuids.size(); i++) {
            System.out.println(uuids.get(i) + " " + sorted.get(i));
        }

        // 4) 정렬 후 순서가 원본 순서와 동일한지 확인
        //    타임베이스드(UUIDv1, v6, v7 등)은 문자열 정렬 시 '시간 순'이 유지되어야 함
        Assertions.assertEquals(uuids, sorted, "문자열 정렬 결과가 생성 순서와 일치하지 않습니다.");

    }
}