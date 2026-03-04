package gift.order;

import gift.member.Member;
import gift.option.Option;
import gift.option.OptionRepository;
import gift.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final OrderMessageClient messageClient;

    public OrderService(
        OrderRepository orderRepository,
        OptionRepository optionRepository,
        OrderMessageClient messageClient
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.messageClient = messageClient;
    }

    public Page<Order> getOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable);
    }

    @Transactional
    public Order createOrder(Member member, Long optionId, int quantity, String message) {
        Option option = optionRepository.findById(optionId)
            .orElseThrow(() -> new NoSuchElementException("옵션이 존재하지 않습니다. id=" + optionId));

        option.subtractQuantity(quantity);

        int price = option.getProduct().getPrice() * quantity;
        member.deductPoint(price);

        Order saved = orderRepository.save(new Order(option, member.getId(), quantity, message));

        sendMessageIfPossible(member, saved, option);
        return saved;
    }

    private void sendMessageIfPossible(Member member, Order order, Option option) {
        if (member.getOAuthAccessToken() == null) {
            return;
        }
        try {
            Product product = option.getProduct();
            messageClient.sendToMe(member.getOAuthAccessToken(), order, product);
        } catch (Exception ignored) {
        }
    }
}
