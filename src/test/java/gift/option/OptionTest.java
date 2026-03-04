package gift.option;

import gift.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionTest {

    @Nested
    @DisplayName("subtractQuantity")
    class SubtractQuantity {

        @Test
        @DisplayName("정상 차감 시 재고가 감소한다")
        void subtractQuantity() {
            Option option = new Option(createProduct(), "옵션A", 10);

            option.subtractQuantity(3);

            assertThat(option.getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("재고와 동일한 수량을 차감하면 재고가 0이 된다")
        void subtractExactQuantity() {
            Option option = new Option(createProduct(), "옵션A", 10);

            option.subtractQuantity(10);

            assertThat(option.getQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("재고보다 많은 수량을 차감하면 예외가 발생한다")
        void subtractMoreThanQuantity() {
            Option option = new Option(createProduct(), "옵션A", 5);

            assertThatThrownBy(() -> option.subtractQuantity(10))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("0개를 차감하면 재고가 변동되지 않는다")
        void subtractZero() {
            Option option = new Option(createProduct(), "옵션A", 10);

            option.subtractQuantity(0);

            assertThat(option.getQuantity()).isEqualTo(10);
        }
    }

    private Product createProduct() {
        return new Product("테스트상품", 1000, "http://image.png", null);
    }
}
