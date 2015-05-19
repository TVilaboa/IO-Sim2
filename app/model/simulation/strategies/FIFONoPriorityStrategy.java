package model.simulation.strategies;

import model.simulation.Customer;
import model.simulation.Event;
import model.simulation.Simulation;
import model.simulation.mathematics.Mathematics;

import javax.validation.constraints.NotNull;

import static model.simulation.Event.EventType.DEPARTURE;
import static model.simulation.Event.Status.EMPTY;
import static model.simulation.Event.Status.OCCUPIED;

/**
 * Created by Megamingus on 16/5/2015.
 */
public class FIFONoPriorityStrategy implements SimulationStrategy {

    @Override public void handleArrival(@NotNull Event event, @NotNull Simulation simulation){

        final Customer customer = event.getCustomer();
        final Customer currentCustomer = simulation.getCurrentCustomer();

        simulation.addCustomertoQueue(customer);
        if (currentCustomer == null) {
            attendNext(event, simulation);
        }

        event.queueLength(simulation.getQueueLength()).attentionChanelStatus(OCCUPIED).setQueueALength(simulation.getALength());
        if (currentCustomer != null) event.setAttentionChannelCustomer(currentCustomer.getType());
    }

    @Override public void handleDeparture(@NotNull Event event, @NotNull Simulation simulation){
        if (!event.isSilent()){
            event.comment("Attended Client");
            attendNext(event, simulation);
        }

        final Customer customer = event.getCustomer();
        customer.setPermanence(event.getInitTime() - customer.getArrivalTime());

        event.queueLength(simulation.getQueueLength()).attentionChanelStatus(simulation.getCurrentCustomer() == null ? EMPTY : OCCUPIED).setQueueALength(simulation.getALength());
        if (simulation.getCurrentCustomer() != null) event.setAttentionChannelCustomer(simulation.getCurrentCustomer().getType());

    }

    @Override public void handleInitiation(@NotNull Event event, @NotNull Simulation simulation){
        event.queueLength(0).attentionChanelStatus(EMPTY).setQueueALength(simulation.getALength());
    }


    private void attendNext(Event event, Simulation simulation) {
        final Customer customer = simulation.pollCustomerQueue();
        simulation.setCurrentCusomer(customer);

        if (customer != null){
            customer.waitTime(event.getInitTime() - customer.getArrivalTime());
            event.attentionChanelStatus(OCCUPIED);
            final double mu = Mathematics.getDurationChannel(simulation.getMuA());
            simulation.addEventAndSort(new Event(DEPARTURE, customer, event.getInitTime() + mu, false));
        } else {
            event.attentionChanelStatus(EMPTY);
        }
    }
}