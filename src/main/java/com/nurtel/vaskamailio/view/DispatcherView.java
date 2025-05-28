package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import com.nurtel.vaskamailio.router.entity.RouterEntity;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "/dispatcher", layout = MainLayout.class)
@PageTitle("Kamailio | Dispatcher")
public class DispatcherView extends VerticalLayout {
    public DispatcherView(DispatcherRepository dispatcherRepository) {
        add("WORK IN PROGRESS");
        Grid<DispatcherEntity> dispatcherEntityGrid = new Grid<>(DispatcherEntity.class, false);
        dispatcherEntityGrid.getStyle().set("height", "80vh");
        add(dispatcherEntityGrid);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getId)
                .setHeader("ID")
                .setWidth("5%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getSetid)
                .setHeader("SetID")
//                .setWidth("5%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getDestination)
                .setHeader("Destination")
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getFlags)
                .setHeader("Flags")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getPriority)
                .setHeader("Priority")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getAttrs)
                .setHeader("Attrs")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getDescription)
                .setHeader("Description")
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addComponentColumn(routerEntity -> {
                    Button editButton = new Button("✏\uFE0F");
                    editButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
                    editButton.getElement().getStyle()
                            .set("font-size", "20px");

                    Div wrapper = new Div(editButton);
                    wrapper.getStyle()
                            .set("display", "flex")
                            .set("justify-content", "center")
                            .set("align-items", "center")
                            .set("height", "100%");

                    return wrapper;
                })
                .setHeader("Редактировать")
                .setWidth("10%")
                .setFlexGrow(0);

        dispatcherEntityGrid.addComponentColumn(routerEntity -> {
                    Button deleteButton = new Button("❌");
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                    deleteButton.getElement().getStyle()
                            .set("font-size", "20px");

                    Div wrapper = new Div(deleteButton);
                    wrapper.getStyle()
                            .set("display", "flex")
                            .set("justify-content", "center")
                            .set("align-items", "center")
                            .set("height", "100%");

                    return wrapper;
                })
                .setHeader("Удалить")
                .setWidth("10%")
                .setFlexGrow(0);

        dispatcherEntityGrid.setItems(dispatcherRepository.findAll());

        dispatcherEntityGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        dispatcherEntityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }
}
