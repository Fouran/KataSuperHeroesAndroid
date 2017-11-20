/*
 * Copyright (C) 2015 Karumi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karumi.katasuperheroes;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.karumi.katasuperheroes.di.MainComponent;
import com.karumi.katasuperheroes.di.MainModule;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.recyclerview.RecyclerViewInteraction;
import com.karumi.katasuperheroes.ui.view.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public DaggerMockRule<MainComponent> daggerRule =
            new DaggerMockRule<>(MainComponent.class, new MainModule()).set(
                    new DaggerMockRule.ComponentSetter<MainComponent>() {
                        @Override
                        public void setComponent(MainComponent component) {
                            SuperHeroesApplication app =
                                    (SuperHeroesApplication) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule
    public IntentsTestRule<MainActivity> activityRule =
            new IntentsTestRule<>(MainActivity.class, true, false);

    @Mock
    SuperHeroesRepository repository;

    @Test
    public void showsEmptyCaseIfThereAreNoSuperHeroes() {
        givenThereAreNoSuperHeroes();

        startActivity();

        onView(withText("¯\\_(ツ)_/¯")).check(matches(isDisplayed()));
    }

    @Test
    public void shouldNotShowEmptyCaseIfThereAreNoSuperHeroes() {
        givenSomeSuperHeroes(10, false);

        startActivity();

        onView(withText("¯\\_(ツ)_/¯")).check(matches(not(isDisplayed())));
    }

    @Test
    public void shouldShowSameNumberOfRowsIntoRecyclerView() {
        givenSomeSuperHeroes(10, false);

        MainActivity mainActivity = startActivity();

        RecyclerView list = (RecyclerView) mainActivity.findViewById(R.id.recycler_view);

        assertEquals(10, list.getAdapter().getItemCount());


    }


    @Test
    public void shouldShowListOfSuperHeroesNames() {
        ArrayList<SuperHero> superHeroes = givenSomeSuperHeroes(3, false);

        startActivity();

        RecyclerViewInteraction.<SuperHero>onRecyclerView(withId(R.id.recycler_view)).withItems(superHeroes).check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
            @Override
            public void check(SuperHero item, View view, NoMatchingViewException e) {


                matches(withText(item.getName())).check(view.findViewById(R.id.tv_super_hero_name), e);
            }
        });
    }


    @Test
    public void shouldShowAvengerBadge() {
        ArrayList<SuperHero> superHeroes = givenSomeSuperHeroes(10, true);

        startActivity();

        RecyclerViewInteraction.<SuperHero>onRecyclerView(withId(R.id.recycler_view)).withItems(superHeroes).check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
            @Override
            public void check(SuperHero item, View view, NoMatchingViewException e) {

                    matches(hasDescendant(allOf(withId(R.id.iv_avengers_badge), isDisplayed()))).check(view, e);

            }
        });


    }

    @Test
    public void shouldNotShowAvengerBadge() {
        ArrayList<SuperHero> superHeroes = givenSomeSuperHeroes(10, false);

        startActivity();

        RecyclerViewInteraction.<SuperHero>onRecyclerView(withId(R.id.recycler_view)).withItems(superHeroes).check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
            @Override
            public void check(SuperHero item, View view, NoMatchingViewException e) {

                matches(hasDescendant(allOf(withId(R.id.iv_avengers_badge), not(isDisplayed())))).check(view, e);

            }
        });
    }



    @Test
    public void JORGE_shouldShowAllElementsWhenSuperHeroesExists() {
        ArrayList<SuperHero> superHeroes = givenSomeSuperHeroes(10, false);

        startActivity();

        RecyclerViewInteraction.<SuperHero>onRecyclerView(withId(R.id.recycler_view)).withItems(superHeroes).check(
                new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
                    @Override
                    public void check(SuperHero item, View view, NoMatchingViewException e) {
                        matches(hasDescendant(withText(item.getName()))).check(view, e);
                    }
                }
        );
    }

    private ArrayList<SuperHero> givenSomeSuperHeroes(int numberOfSuperHeroes, boolean isAvengers) {
        ArrayList<SuperHero> superHeroes = new ArrayList<>();

        for (int i = 0; i < numberOfSuperHeroes; ++i) {
            SuperHero superHero = new SuperHero("name " + i, null, isAvengers, "description " + i);

            superHeroes.add(superHero);
        }

        when(repository.getAll()).thenReturn(superHeroes);

        return superHeroes;

    }

    private void givenThereAreNoSuperHeroes() {
        when(repository.getAll()).thenReturn(Collections.<SuperHero>emptyList());
    }

    private MainActivity startActivity() {
        return activityRule.launchActivity(null);
    }
}