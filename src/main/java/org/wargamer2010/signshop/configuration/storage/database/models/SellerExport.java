package org.wargamer2010.signshop.configuration.storage.database.models;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class SellerExport {
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "seller_id", referencedColumnName = "id")
    private Seller seller;

    @ElementCollection
    @Column(name = "export_containables")
    private List<String> containables;

    @ElementCollection
    @Column(name = "export_activatables")
    private List<String> activatables;

    @ElementCollection
    @Column(name = "export_items")
    private List<String> items;

    @Column(name = "export_state")
    private String state;

    protected SellerExport() {}
    protected SellerExport(Seller seller, List<String> containables, List<String> activatables, List<String> items, String state) {
        this.seller = seller;
        this.containables = containables;
        this.activatables = activatables;
        this.items = items;
        this.state = state;
    }

    public static SellerExport export(Seller seller, String state) {
        List<String> containables = seller.getContainables().stream().map((block) -> signshopUtil.convertLocationToString(block.getLocation())).collect(Collectors.toList());
        List<String> activatables = seller.getActivatables().stream().map((block) -> signshopUtil.convertLocationToString(block.getLocation())).collect(Collectors.toList());
        List<String> items = Arrays.stream(seller.getItems(false)).map((stack) -> String.format("%d %s", stack.getAmount(), stack.getType().name())).collect(Collectors.toList());

        return new SellerExport(seller, containables, activatables, items, state);
    }
}
