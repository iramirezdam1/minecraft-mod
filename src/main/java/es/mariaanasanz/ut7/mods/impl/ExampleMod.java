package es.mariaanasanz.ut7.mods.impl;

import es.mariaanasanz.ut7.mods.base.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.swing.text.html.parser.DTD;
import java.util.Iterator;

/* TreeDescender: (Danna e Ítalo)
Crea un mod que permita al jugador descender el contenido de un árbol al romper cualquiera
de los bloques del árbol CON UNA PALA. Deberás tener en cuenta que un árbol está compuesto
por troncos y hojas. ¡Cuidado! Si un árbol está en contacto con otro, deberás descender
únicamente los bloques del árbol que estás talando, y no los bloques del otro árbol.
¡Cuidado! Si el árbol tiene algún bloque adicional en contacto, no deberás hacer nada
con esos bloques. Deberás tener en cuenta que el árbol se ha quedado "en el aire" para
descender sus bloques. Únicamente podrás descender sus bloques si todos los bloques que
vayas a descender tengan aire debajo. */
@Mod(DamMod.MOD_ID)
public class ExampleMod extends DamMod implements IBlockBreakEvent, IServerStartEvent,
        IItemPickupEvent, ILivingDamageEvent, IUseItemEvent, IFishedEvent,
        IInteractEvent, IMovementEvent {
    private Player jugador;
    private Level mundo;
    private  MovementInputUpdateEvent eventoMovimientoJugador;
    private PlayerInteractEvent eventoClickIzquierdo;

    public ExampleMod(){
        super();
    }

    @Override
    public String autor() {
        return "Javier Jorge Soteras";
    }

    @Override
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        System.out.println("Se Rompio el bloque");
        BlockPos pos = event.getPos();
        System.out.println("Posicion del bloque roto" + pos.toString());
        Block BloqueRoto = event.getState().getBlock();
        System.out.println("Bloque destruido en la posicion " + pos);
        if (tientePala()) {
            if (BloqueRoto.equals(Blocks.ACACIA_LOG) || BloqueRoto.equals(Blocks.BIRCH_LOG) ||
                    BloqueRoto.equals(Blocks.DARK_OAK_LOG) || BloqueRoto.equals(Blocks.JUNGLE_LOG) ||
                    BloqueRoto.equals(Blocks.OAK_LOG) || BloqueRoto.equals(Blocks.SPRUCE_LOG)) {
                System.out.println("Era Madera");
                //For para llamar a treeAreaClone x veces
                for (int i = 0; i < 10; i++) {
                    treeAreaClone(new BlockPos(pos.getX(), pos.getY() + i, pos.getZ()));

                }


            }

        }

    }

    /*Analiza el area Bidimensional de el bloque roto*/
    public void treeAreaClone(BlockPos pos){
        System.out.println("Se ha entrado a treeArea");
        double radio = 6;
        BlockPos bloqueEsquina = new BlockPos(pos.getX()-radio,pos.getY(),pos.getZ()-radio);
        System.out.println("Bloque de la esquina"+bloqueEsquina.toString());
        int x = bloqueEsquina.getX();
        int y = bloqueEsquina.getY();
        int z = bloqueEsquina.getZ();
        for (int i = x; i < x+(radio*2+1); i++) {
            for (int j = z; j < z+(radio*2+1); j++) {
                // System.out.println("se ha analizado la casilla"+i+","+j);

                //obtener el bloque de la posicion analizada
                BlockPos posBlockAnalizado = new BlockPos(i,y,j);
                BlockState  estadoBloqueAnalizado = Minecraft.getInstance().level.getBlockState(posBlockAnalizado);
                Block bloqueAnalizado = estadoBloqueAnalizado.getBlock();
                System.out.println("El bloque analizado es" + bloqueAnalizado.toString());

                // Posicion del bloque de arriba
                colocarBloque(posBlockAnalizado, estadoBloqueAnalizado,bloqueArriba(posBlockAnalizado));
            }
        }
    }

    /*Metodo para obtener el bloque arriba del bloque de la posicion pasada*/
    public BlockState bloqueArriba(BlockPos pos){
        System.out.println("Se ha entrado en bloqueAriba");
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        // Suma la posicion "Y" para obtener el bloque de arriba
        BlockPos posicionArriba = new BlockPos(x,y + 1,z);
        // Return bloque
        return Minecraft.getInstance().level.getBlockState(posicionArriba);
    }

    /* Colocar bloque en pos y tipo bloque pasado*/
    public void colocarBloque(BlockPos pos, BlockState estadoBloqueAnalizado,BlockState estadoBloqueArriba){

        Minecraft.getInstance().level.setBlockAndUpdate(pos, estadoBloqueArriba);
        eventoMovimientoJugador.getEntity().getLevel().setBlockAndUpdate(pos, estadoBloqueArriba);
        Minecraft.getInstance().level.setBlock(pos, estadoBloqueArriba,512);
        eventoMovimientoJugador.getEntity().getLevel().setBlock(pos, estadoBloqueArriba,512);
        mundo.onBlockStateChange(pos,estadoBloqueAnalizado,estadoBloqueArriba);
        mundo.sendBlockUpdated(pos,estadoBloqueAnalizado,estadoBloqueArriba,3);


    }
    @SubscribeEvent
    public void clickIzuqierdo(PlayerInteractEvent.LeftClickBlock event){eventoClickIzquierdo = event;
        tientePala();
    }

    public boolean tientePala() {
        boolean tienePala=false;
        //System.out.println(jugador.getHandSlots().toString());
        Iterator<ItemStack> it = jugador.getHandSlots().iterator();
        while (it.hasNext()) {
            ItemStack item = it.next();
            if(item.is(Items.WOODEN_SHOVEL)){
                System.out.println("ESTA DANDO CLICK CON LA PALA");
                tienePala=true;}
        }

        return tienePala;
    }


    @Override
    @SubscribeEvent
    public void onServerStart(ServerStartingEvent event) {
        LOGGER.info("Server starting");

    }

    @Override
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        LOGGER.info("Item recogido");
        System.out.println("Item recogido");
    }

    @Override
    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        System.out.println("evento LivingDamageEvent invocado "+event.getEntity().getClass()+" provocado por "+event.getSource().getEntity());
    }

    @Override
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        System.out.println("evento LivingDeathEvent invocado "+event.getEntity().getClass()+" provocado por "+event.getSource().getEntity());

    }

    @Override
    @SubscribeEvent
    public void onUseItem(LivingEntityUseItemEvent event) {
        LOGGER.info("evento LivingEntityUseItemEvent invocado "+event.getEntity().getClass());
    }


    @Override
    @SubscribeEvent
    public void onPlayerFish(ItemFishedEvent event) {
        System.out.println("¡Has pescado un pez!");
    }

    @Override
    @SubscribeEvent
    public void onPlayerTouch(PlayerInteractEvent.RightClickBlock event) {
        System.out.println("¡Has hecho click derecho!");
        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        if (ItemStack.EMPTY.equals(heldItem)) {
            System.out.println("La mano esta vacia");
            if (state.getBlock().getName().getString().trim().toLowerCase().endsWith("log")) {
                System.out.println("¡Has hecho click sobre un tronco!");
            }
        }
    }


    @Override
    @SubscribeEvent
    public void onPlayerWalk(MovementInputUpdateEvent event) {
        mundo = jugador.getCommandSenderWorld();
        jugador = event.getEntity();
        eventoMovimientoJugador = event;


        if(event.getEntity() instanceof Player){
            if(event.getInput().down){
                System.out.println("down"+event.getInput().down);
            }
            if(event.getInput().up){
                System.out.println("up"+event.getInput().up);
            }
            if(event.getInput().right){
                System.out.println("right"+event.getInput().right);
            }
            if(event.getInput().left){
                System.out.println("left"+event.getInput().left);
            }
        }
    }
}
